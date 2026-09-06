package com.example.bluetoothgamepad.viewmodel

import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetoothgamepad.bluetooth.*
import com.example.bluetoothgamepad.data.*
import com.example.bluetoothgamepad.domain.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.os.Build
import kotlinx.coroutines.delay

class GamepadViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = SettingsRepository(app)
    val settings = repository.settings.stateIn(viewModelScope, SharingStarted.Eagerly, GamepadSettings())
    private val _gamepadState = MutableStateFlow(GamepadState())
    val gamepadState = _gamepadState.asStateFlow()
    private val _connection = MutableStateFlow<ConnectionState>(ConnectionState.Ready)
    val connection = _connection.asStateFlow()
    private val _devices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val devices = _devices.asStateFlow()
    private var service: HidGamepadService? = null
    private var connectionJob: kotlinx.coroutines.Job? = null
    private var deviceJob: kotlinx.coroutines.Job? = null

    fun attach(service: HidGamepadService) {
        this.service = service
        service.startGamepad(ProfileKind.fromId(settings.value.selectedProfile).profile)
        connectionJob?.cancel()
        connectionJob = viewModelScope.launch { service.connectionState.collect { _connection.value = it; refreshDevices() } }
        deviceJob?.cancel()
        deviceJob = viewModelScope.launch { service.nearbyDevices.collect { refreshDevices() } }
        refreshDevices()
    }
    fun detach() { service = null; connectionJob?.cancel();deviceJob?.cancel() }
    fun refreshDevices() { _devices.value = service?.allDevices().orEmpty() }
    fun searchDevices() { service?.searchDevices();refreshDevices() }
    fun connect(device: BluetoothDevice) { service?.connect(device); viewModelScope.launch { repository.setLastHost(device.address) } }
    fun disconnect() { service?.disconnect() }
    fun update(transform: (GamepadState) -> GamepadState) {
        val before=_gamepadState.value;_gamepadState.update(transform);val after=_gamepadState.value
        if(settings.value.hapticsEnabled && isNewPress(before,after)) vibrate()
        service?.sendState(after)
    }
    fun neutralize() { _gamepadState.value = GamepadState(); service?.sendState(GamepadState()) }
    fun moveMouse(dx:Int,dy:Int,wheel:Int=0){service?.sendMouse(dx=dx,dy=dy,wheel=wheel)}
    fun clickMouse(button:Int){viewModelScope.launch{service?.sendMouse(buttons=button);delay(35);service?.sendMouse()}}
    fun pressTvKey(keyCode:Int){viewModelScope.launch{service?.sendKey(keyCode);delay(45);service?.sendKey(0)}}
    fun setProfile(kind: ProfileKind) { viewModelScope.launch { repository.setProfile(kind.profile.id); service?.startGamepad(kind.profile) } }
    fun saveSettings(s: GamepadSettings) { viewModelScope.launch { repository.update(s.stickDeadZone,s.stickSensitivity,s.invertLeftY,s.invertRightY,s.hapticsEnabled,s.darkTheme,s.triggerStep,s.stickHoldDelayMs,s.touchpadSpeed,s.touchpadScrollLines,s.touchpadDoubleTapMs,s.landscapeLocked) } }
    fun setLanguage(language: String) { viewModelScope.launch { repository.setLanguage(language) } }
    fun saveAppearance(backgroundUri: String?, controlColor: Long, zoom:Float, offsetX:Float, offsetY:Float, automaticTextColor:Boolean, textColor:Long, backgroundColor:Long, darkTheme:Boolean, buttonLabelColor:Long, dpadLineColor:Long) { viewModelScope.launch { repository.setAppearance(backgroundUri,controlColor,zoom,offsetX,offsetY,automaticTextColor,textColor,backgroundColor,darkTheme,buttonLabelColor,dpadLineColor) } }
    private fun isNewPress(a:GamepadState,b:GamepadState)=(!a.south&&b.south)||(!a.east&&b.east)||(!a.west&&b.west)||(!a.north&&b.north)||(!a.l1&&b.l1)||(!a.r1&&b.r1)||(!a.l3&&b.l3)||(!a.r3&&b.r3)||(!a.start&&b.start)||(!a.select&&b.select)||(!a.home&&b.home)||(a.leftTrigger==0f&&b.leftTrigger>0f)||(a.rightTrigger==0f&&b.rightTrigger>0f)||(a.dpad==DPadDirection.CENTER&&b.dpad!=DPadDirection.CENTER)
    private fun vibrate(){val app=getApplication<Application>();val vibrator=if(Build.VERSION.SDK_INT>=31)app.getSystemService(VibratorManager::class.java).defaultVibrator else app.getSystemService(Vibrator::class.java);vibrator.vibrate(VibrationEffect.createOneShot(22,VibrationEffect.DEFAULT_AMPLITUDE))}
}
