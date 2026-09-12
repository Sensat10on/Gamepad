package com.example.bluetoothgamepad.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.bluetoothgamepad.MainActivity
import com.example.bluetoothgamepad.R
import com.example.bluetoothgamepad.domain.GamepadProfile
import com.example.bluetoothgamepad.domain.GamepadState
import com.example.bluetoothgamepad.domain.StandardGamepadProfile
import com.example.bluetoothgamepad.domain.KeyboardReport
import com.example.bluetoothgamepad.domain.MouseProfile
import com.example.bluetoothgamepad.ui.strings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch

/**
 * Owns the Bluetooth HID Device proxy, the SDP record, the report pipeline and the
 * foreground notification. Callback connection events — not the return value of
 * `connect()` — are the authoritative source of [ConnectionState].
 */
class HidGamepadService : Service() {

    inner class LocalBinder : Binder() { val service get() = this@HidGamepadService }

    private val binder = LocalBinder()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Ready)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private var manager: HidConnectionManager? = null

    /** Incremented whenever the proxy is replaced or closed, to invalidate late callbacks. */
    private var managerGeneration = 0
    private var profile: GamepadProfile = StandardGamepadProfile
    private var language = "ru"
    private var destroyed = false

    /** Newest requested pad state; the sender below coalesces it to at most 100 Hz. */
    private val requestedState = MutableStateFlow(GamepadState())

    /** Forces an immediate re-send of [requestedState] (used right after a host connects). */
    private val resendRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /** Only ever touched from the sender coroutine, so no synchronization is required. */
    private var lastSent: GamepadState? = null
    private var sender: Job? = null

    private val _nearbyDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val nearbyDevices: StateFlow<List<BluetoothDevice>> = _nearbyDevices.asStateFlow()
    private var pendingPairAddress: String? = null
    private var receiverRegistered = false

    private val bluetoothReceiver = object : BroadcastReceiver() {
        // Everything below touches BluetoothDevice accessors that need BLUETOOTH_CONNECT, so the
        // permission is verified first and the receiver simply goes quiet without it.
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            if (!hasBluetoothPermission()) return
            val device = if (Build.VERSION.SDK_INT >= 33) {
                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
            } else {
                @Suppress("DEPRECATION") intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            }
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> device?.let { addNearby(it) }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    device ?: return
                    when (device.bondState) {
                        BluetoothDevice.BOND_BONDED -> {
                            addNearby(device)
                            if (device.address == pendingPairAddress) {
                                pendingPairAddress = null
                                connect(device)
                            }
                        }
                        BluetoothDevice.BOND_NONE -> {
                            // Pairing failed or was cancelled: do not leave a stale address behind,
                            // otherwise an unrelated later pairing would auto-connect this device.
                            if (device.address == pendingPairAddress) pendingPairAddress = null
                        }
                    }
                }
            }
        }
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createChannel()
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        }
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(bluetoothReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION") registerReceiver(bluetoothReceiver, filter)
        }
        receiverRegistered = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopGamepad()
            return START_NOT_STICKY
        }
        // Only reaches here when MainActivity verified BLUETOOTH_CONNECT; the guard also keeps a
        // revoked permission from turning into an uncatchable crash on Android 14+.
        if (!hasBluetoothPermission()) {
            _connectionState.value = ConnectionState.Error("Bluetooth permission required")
            stopSelf()
            return START_NOT_STICKY
        }
        startForegroundSafely(notification(notificationText(_connectionState.value)))
        return START_NOT_STICKY
    }

    /**
     * Applies the selected profile. Idempotent: safe to call on every settings change and on
     * every activity (re)attach.
     */
    fun startGamepad(selected: GamepadProfile) {
        profile = selected
        if (!hasBluetoothPermission()) {
            _connectionState.value = ConnectionState.Error("Bluetooth permission required")
            return
        }
        if (manager != null) return
        startForegroundSafely(notification(notificationText(ConnectionState.Ready)))
        val adapter = bluetoothAdapter()
        if (adapter == null) {
            _connectionState.value = ConnectionState.Error("Bluetooth is unavailable")
            return
        }
        managerGeneration++
        val generation = managerGeneration
        val created = HidConnectionManager(this, adapter, mainExecutor) { state ->
            // A manager that has already been replaced (or closed) must not report into the current
            // session: unregisterApp() during close() would otherwise surface as a bogus
            // "HID registration lost" error on top of a freshly created one.
            if (generation != managerGeneration || destroyed) return@HidConnectionManager
            _connectionState.value = state
            if (state is ConnectionState.Connected) resendRequests.tryEmit(Unit)
            updateNotification(state)
        }
        manager = created
        created.start()

        sender?.cancel()
        sender = scope.launch { sendLoop() }
    }

    fun setLanguage(code: String) {
        if (language == code) return
        language = code
        if (!destroyed) updateNotification(_connectionState.value)
    }

    @OptIn(FlowPreview::class)
    private suspend fun sendLoop() {
        val coalesced = requestedState.sample(SEND_INTERVAL_MS).distinctUntilChanged().map { it to false }
        val forced = resendRequests.map { requestedState.value to true }
        merge(coalesced, forced).collect { (state, force) ->
            if (!force && state == lastSent) return@collect
            if (manager?.send(profile.reportId, profile.encode(state)) == true) lastSent = state
        }
    }

    fun bondedDevices(): List<BluetoothDevice> = if (hasBluetoothPermission()) manager?.bondedDevices().orEmpty() else emptyList()

    /** Returns an empty list once BLUETOOTH_CONNECT is gone; device.address would throw otherwise. */
    fun allDevices(): List<BluetoothDevice> {
        if (!hasBluetoothPermission()) return emptyList()
        return (bondedDevices() + nearbyDevices.value).distinctBy { it.address }.sortedBy { deviceLabel(it) }
    }

    /** Guarded by an explicit [hasBluetoothPermission] check above. */
    @SuppressLint("MissingPermission")
    fun searchDevices(): Boolean {
        if (!hasBluetoothPermission()) return false
        val adapter = bluetoothAdapter() ?: return false
        _nearbyDevices.value = emptyList()
        if (adapter.isDiscovering) adapter.cancelDiscovery()
        return adapter.startDiscovery()
    }

    /** Guarded by an explicit [hasBluetoothPermission] check above. */
    @SuppressLint("MissingPermission")
    fun connect(device: BluetoothDevice): Boolean {
        if (!hasBluetoothPermission()) return false
        // Discovery and connection share the Bluetooth controller: leaving discovery running is a
        // classic cause of rejected or very slow connections.
        runCatching { bluetoothAdapter()?.takeIf { it.isDiscovering }?.cancelDiscovery() }
        if (device.bondState != BluetoothDevice.BOND_BONDED) {
            pendingPairAddress = device.address
            return runCatching { device.createBond() }.getOrDefault(false)
        }
        // Bluetooth may have been off when the session started; rebuild the proxy in that case.
        if (manager?.isRegistered != true) {
            manager?.close()
            manager = null
            startGamepad(profile)
        }
        return manager?.connect(device) == true
    }

    fun disconnect(): Boolean = hasBluetoothPermission() && manager?.disconnect() == true

    fun sendState(state: GamepadState) { requestedState.value = state }

    fun sendMouse(buttons: Int = 0, dx: Int = 0, dy: Int = 0, wheel: Int = 0): Boolean =
        profile === MouseProfile && manager?.send(MouseProfile.reportId, MouseProfile.encodeMouse(buttons, dx, dy, wheel)) == true

    fun sendKey(keyCode: Int): Boolean =
        profile === MouseProfile && manager?.send(KeyboardReport.REPORT_ID, KeyboardReport.encode(keyCode)) == true

    /** Tears the whole session down: neutral report, disconnect, unregister, drop the notification. */
    fun stopGamepad() {
        // Sent synchronously: the proxy is closed immediately below, so a coroutine would race it.
        runCatching { manager?.send(profile.reportId, profile.encode(GamepadState())) }
        managerGeneration++
        sender?.cancel()
        sender = null
        manager?.close()
        manager = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        getSystemService(NotificationManager::class.java).cancel(NOTIFICATION_ID)
        stopSelf()
    }

    override fun onDestroy() {
        destroyed = true
        managerGeneration++
        sender?.cancel()
        manager?.close()
        manager = null
        getSystemService(NotificationManager::class.java).cancel(NOTIFICATION_ID)
        if (receiverRegistered) {
            runCatching { unregisterReceiver(bluetoothReceiver) }
            receiverRegistered = false
        }
        scope.cancel()
        super.onDestroy()
    }

    private fun addNearby(device: BluetoothDevice) {
        if (!hasBluetoothPermission()) return
        _nearbyDevices.value = (_nearbyDevices.value + device).distinctBy { it.address }.sortedBy { deviceLabel(it) }
    }

    private fun bluetoothAdapter(): BluetoothAdapter? =
        runCatching { getSystemService(BluetoothManager::class.java)?.adapter }.getOrNull()

    private fun hasBluetoothPermission(): Boolean =
        Build.VERSION.SDK_INT < 31 || checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED

    /**
     * Every BluetoothDevice accessor that needs BLUETOOTH_CONNECT is funnelled through here, so a
     * revoked permission degrades to a placeholder instead of a SecurityException.
     */
    @SuppressLint("MissingPermission")
    private fun deviceLabel(device: BluetoothDevice): String {
        if (!hasBluetoothPermission()) return "—"
        return runCatching { device.name ?: device.address }.getOrDefault("—")
    }

    private fun createChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "Gamepad session", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun startForegroundSafely(value: Notification) {
        try {
            startForeground(NOTIFICATION_ID, value)
        } catch (t: Throwable) {
            // Android 14+ rejects a connectedDevice foreground service without BLUETOOTH_CONNECT.
            // Failing here must not crash the process; report and give up on the session.
            Log.e(TAG, "startForeground() failed", t)
            _connectionState.value = ConnectionState.Error("Foreground service start was rejected")
            stopSelf()
        }
    }

    private fun notificationText(state: ConnectionState): String {
        val text = strings(language)
        return when (state) {
            is ConnectionState.Connected -> "${text.connected}: ${deviceLabel(state.device)}"
            is ConnectionState.Connecting -> text.connecting
            is ConnectionState.Error -> state.message
            ConnectionState.BluetoothOff -> text.bluetoothOff
            ConnectionState.Registering -> text.registering
            ConnectionState.Disconnecting -> text.disconnecting
            else -> text.ready
        }
    }

    private fun notification(value: String): Notification {
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val stop = PendingIntent.getService(
            this, 1, Intent(this, HidGamepadService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_gamepad)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(value)
            .setOngoing(true)
            .setContentIntent(open)
            .addAction(0, strings(language).stop, stop)
            .build()
    }

    private fun updateNotification(state: ConnectionState) {
        if (destroyed) return
        getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, notification(notificationText(state)))
    }

    companion object {
        const val CHANNEL_ID = "gamepad_session"
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP = "com.example.bluetoothgamepad.action.STOP"

        /** 10 ms => at most 100 coalesced reports per second. */
        private const val SEND_INTERVAL_MS = 10L
        private const val TAG = "BluetoothGamepad"
    }
}
