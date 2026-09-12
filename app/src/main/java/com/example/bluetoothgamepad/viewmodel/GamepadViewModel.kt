package com.example.bluetoothgamepad.viewmodel

import android.app.Application
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetoothgamepad.bluetooth.ConnectionState
import com.example.bluetoothgamepad.bluetooth.HidGamepadService
import com.example.bluetoothgamepad.data.GamepadSettings
import com.example.bluetoothgamepad.data.SettingsRepository
import com.example.bluetoothgamepad.domain.DPadDirection
import com.example.bluetoothgamepad.domain.GamepadState
import com.example.bluetoothgamepad.domain.ProfileKind
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Why the HID session is not available; surfaced as a banner on the connection screen. */
enum class StartupBlocker { PERMISSION, SERVICE }

class GamepadViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = SettingsRepository(app)
    val settings = repository.settings.stateIn(viewModelScope, SharingStarted.Eagerly, GamepadSettings())

    private val _gamepadState = MutableStateFlow(GamepadState())
    val gamepadState = _gamepadState.asStateFlow()
    private val _connection = MutableStateFlow<ConnectionState>(ConnectionState.Ready)
    val connection = _connection.asStateFlow()
    private val _devices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val devices = _devices.asStateFlow()
    private val _blocker = MutableStateFlow<StartupBlocker?>(null)
    val blocker = _blocker.asStateFlow()

    // The bound service is referenced only while the activity is attached; [detach] (called from
    // onServiceDisconnected and onCleared) drops it again, so no activity Context is retained.
    @SuppressLint("StaticFieldLeak")
    private var service: HidGamepadService? = null

    private var connectionJob: Job? = null
    private var deviceJob: Job? = null
    private var settingsJob: Job? = null

    /** Bitmask of currently held mouse buttons, so pointer motion can drag while a button is down. */
    private var mouseButtons = 0

    fun attach(service: HidGamepadService) {
        this.service = service
        connectionJob?.cancel()
        deviceJob?.cancel()
        settingsJob?.cancel()
        connectionJob = viewModelScope.launch {
            service.connectionState.collect { _connection.value = it; refreshDevices() }
        }
        deviceJob = viewModelScope.launch { service.nearbyDevices.collect { refreshDevices() } }
        // The persisted profile must not be read from settings.value at attach time: DataStore may
        // not have loaded yet and the default ("generic") would win, leaving the mouse profile
        // permanently inactive. Collecting re-applies the profile as soon as the real value lands.
        settingsJob = viewModelScope.launch {
            settings.collect { current ->
                service.startGamepad(ProfileKind.fromId(current.selectedProfile).profile)
                service.setLanguage(current.language)
            }
        }
        refreshDevices()
    }

    fun detach() {
        service = null
        connectionJob?.cancel()
        deviceJob?.cancel()
        settingsJob?.cancel()
    }

    override fun onCleared() {
        detach()
        super.onCleared()
    }

    fun refreshDevices() { _devices.value = service?.allDevices().orEmpty() }
    fun searchDevices() { service?.searchDevices(); refreshDevices() }
    fun setBlocker(blocker: StartupBlocker?) { _blocker.value = blocker }

    fun connect(device: BluetoothDevice) {
        service?.connect(device)
        viewModelScope.launch { repository.setLastHost(device.address) }
    }

    fun disconnect() { service?.disconnect() }

    /** Ends the whole HID session: neutral report, disconnect, unregister, drop the notification. */
    fun stopSession() {
        neutralize()
        service?.stopGamepad()
    }

    fun update(transform: (GamepadState) -> GamepadState) {
        val before = _gamepadState.value
        _gamepadState.update(transform)
        val after = _gamepadState.value
        if (settings.value.hapticsEnabled && isNewPress(before, after)) vibrate()
        service?.sendState(after)
    }

    /** Returns every control to neutral; called when the UI goes away or is stopped. */
    fun neutralize() {
        _gamepadState.value = GamepadState()
        mouseButtons = 0
        service?.sendState(GamepadState())
        service?.sendMouse()
    }

    fun moveMouse(dx: Int, dy: Int, wheel: Int = 0) { service?.sendMouse(mouseButtons, dx, dy, wheel) }

    /** Press/release of a mouse button; holding one while moving the pad produces a drag. */
    fun setMouseButton(button: Int, pressed: Boolean) {
        mouseButtons = if (pressed) mouseButtons or button else mouseButtons and button.inv()
        service?.sendMouse(buttons = mouseButtons)
    }

    fun pressTvKey(keyCode: Int) {
        viewModelScope.launch {
            service?.sendKey(keyCode)
            delay(TV_KEY_HOLD_MS)
            service?.sendKey(0)
        }
    }

    fun setProfile(kind: ProfileKind) {
        viewModelScope.launch { repository.setProfile(kind.profile.id) }
    }

    fun saveSettings(s: GamepadSettings) {
        viewModelScope.launch {
            repository.update(s.stickDeadZone, s.stickSensitivity, s.invertLeftY, s.invertRightY, s.hapticsEnabled, s.darkTheme, s.triggerStep, s.stickHoldDelayMs, s.touchpadSpeed, s.touchpadScrollLines, s.touchpadDoubleTapMs, s.landscapeLocked)
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch { repository.setLanguage(language) }
    }

    /** Persists the appearance draft produced by `AppearanceScreen`. */
    fun saveAppearance(updated: GamepadSettings) {
        viewModelScope.launch { repository.setAppearance(updated) }
    }

    private fun isNewPress(a: GamepadState, b: GamepadState) = (!a.south && b.south) || (!a.east && b.east) || (!a.west && b.west) || (!a.north && b.north) || (!a.l1 && b.l1) || (!a.r1 && b.r1) || (!a.l3 && b.l3) || (!a.r3 && b.r3) || (!a.start && b.start) || (!a.select && b.select) || (!a.home && b.home) || (a.leftTrigger == 0f && b.leftTrigger > 0f) || (a.rightTrigger == 0f && b.rightTrigger > 0f) || (a.dpad == DPadDirection.CENTER && b.dpad != DPadDirection.CENTER)

    private fun vibrate() {
        val app = getApplication<Application>()
        val vibrator = if (Build.VERSION.SDK_INT >= 31) {
            app.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION") app.getSystemService(Vibrator::class.java)
        } ?: return
        runCatching { vibrator.vibrate(VibrationEffect.createOneShot(22, VibrationEffect.DEFAULT_AMPLITUDE)) }
    }

    private companion object {
        /** Android TV hosts routinely miss shorter key taps. */
        const val TV_KEY_HOLD_MS = 80L
    }
}
