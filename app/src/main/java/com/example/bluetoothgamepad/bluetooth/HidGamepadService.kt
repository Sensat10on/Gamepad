package com.example.bluetoothgamepad.bluetooth

import android.Manifest
import android.app.*
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.bluetoothgamepad.MainActivity
import com.example.bluetoothgamepad.R
import com.example.bluetoothgamepad.domain.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HidGamepadService : Service() {
    inner class LocalBinder : Binder() { val service get() = this@HidGamepadService }
    private val binder = LocalBinder()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Ready)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    private var manager: HidConnectionManager? = null
    private var profile: GamepadProfile = GenericHidProfile
    private var pending: GamepadState? = null
    private var lastState: GamepadState? = null
    private var sender: Job? = null
    private val _nearbyDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val nearbyDevices: StateFlow<List<BluetoothDevice>> = _nearbyDevices.asStateFlow()
    private var pendingPairAddress: String? = null
    private var receiverRegistered = false
    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val device = if (Build.VERSION.SDK_INT >= 33) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java) else @Suppress("DEPRECATION") intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> device?.let { addNearby(it) }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> if (device?.bondState == BluetoothDevice.BOND_BONDED) {
                    addNearby(device)
                    if (device.address == pendingPairAddress) { pendingPairAddress = null; manager?.connect(device) }
                }
            }
        }
    }

    override fun onBind(intent: Intent): IBinder = binder
    override fun onCreate() {
        super.onCreate(); createChannel()
        val filter=IntentFilter().apply { addAction(BluetoothDevice.ACTION_FOUND);addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED) }
        if(Build.VERSION.SDK_INT>=33) registerReceiver(bluetoothReceiver,filter,RECEIVER_EXPORTED) else @Suppress("DEPRECATION") registerReceiver(bluetoothReceiver,filter)
        receiverRegistered=true
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, notification("Starting HID service…"))
        return START_NOT_STICKY
    }

    fun startGamepad(selected: GamepadProfile) {
        if (!hasBluetoothPermission()) { _connectionState.value = ConnectionState.Error("Bluetooth permission required"); return }
        profile = selected
        pending=null;lastState=null
        if(manager!=null) return
        startForeground(NOTIFICATION_ID, notification("Ready to connect"))
        val adapter = getSystemService(BluetoothManager::class.java).adapter
        if (adapter == null) { _connectionState.value = ConnectionState.Error("Bluetooth is unavailable"); return }
        manager = HidConnectionManager(this, adapter, mainExecutor) { state ->
            _connectionState.value = state
            updateNotification(state)
        }.also { it.start(profile) }
        sender?.cancel()
        sender = scope.launch {
            while (isActive) {
                val state = pending
                if (state != null && state != lastState) {
                    if (manager?.send(profile.reportId, profile.encode(state)) == true) lastState = state
                }
                delay(10) // coalescing rate limiter: at most 100 reports/second
            }
        }
    }
    fun bondedDevices() = if (hasBluetoothPermission()) manager?.bondedDevices().orEmpty() else emptyList()
    fun allDevices() = (bondedDevices()+nearbyDevices.value).distinctBy{it.address}.sortedBy{it.name?:it.address}
    fun searchDevices(): Boolean {
        if(!hasBluetoothPermission()) return false
        val adapter=getSystemService(BluetoothManager::class.java).adapter?:return false
        _nearbyDevices.value=emptyList()
        if(adapter.isDiscovering) adapter.cancelDiscovery()
        return adapter.startDiscovery()
    }
    fun connect(device: BluetoothDevice): Boolean {
        if(!hasBluetoothPermission()) return false
        return if(device.bondState==BluetoothDevice.BOND_BONDED) manager?.connect(device)==true else {
            pendingPairAddress=device.address
            device.createBond()
        }
    }
    fun disconnect() = hasBluetoothPermission() && manager?.disconnect() == true
    fun sendState(state: GamepadState) { pending = state }
    fun sendMouse(buttons:Int=0,dx:Int=0,dy:Int=0,wheel:Int=0)=profile===MouseProfile && manager?.send(MouseProfile.reportId,MouseProfile.encodeMouse(buttons,dx,dy,wheel))==true
    fun sendKey(keyCode:Int)=profile===MouseProfile && manager?.send(KeyboardReport.REPORT_ID,KeyboardReport.encode(keyCode))==true
    fun stopGamepad() { sender?.cancel(); manager?.close(); manager = null; stopForeground(STOP_FOREGROUND_REMOVE); stopSelf() }
    override fun onDestroy() { sender?.cancel(); manager?.close(); if(receiverRegistered) unregisterReceiver(bluetoothReceiver); scope.cancel(); super.onDestroy() }

    private fun addNearby(device:BluetoothDevice){_nearbyDevices.value=(_nearbyDevices.value+device).distinctBy{it.address}.sortedBy{it.name?:it.address}}

    private fun hasBluetoothPermission() = Build.VERSION.SDK_INT < 31 || checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
    private fun createChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "Gamepad session", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
    private fun notification(text: String): Notification {
        val pendingIntent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Builder(this, CHANNEL_ID).setSmallIcon(R.drawable.ic_gamepad)
            .setContentTitle("Bluetooth Gamepad").setContentText(text).setOngoing(true).setContentIntent(pendingIntent).build()
    }
    private fun updateNotification(state: ConnectionState) {
        val text = when (state) {
            is ConnectionState.Connected -> "Connected to ${state.device.name ?: state.device.address}"
            is ConnectionState.Connecting -> "Connecting…"
            is ConnectionState.Error -> state.message
            ConnectionState.BluetoothOff -> "Bluetooth is off"
            else -> "Ready to connect"
        }
        getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, notification(text))
    }
    companion object { const val CHANNEL_ID = "gamepad_session"; const val NOTIFICATION_ID = 1001 }
}
