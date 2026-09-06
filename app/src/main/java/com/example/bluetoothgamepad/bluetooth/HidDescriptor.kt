package com.example.bluetoothgamepad.bluetooth

import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import com.example.bluetoothgamepad.domain.GamepadProfile
import com.example.bluetoothgamepad.domain.HidReportEncoder
import com.example.bluetoothgamepad.domain.MouseProfile
import com.example.bluetoothgamepad.domain.KeyboardReport

object HidDescriptor {
    val combinedBytes:ByteArray=com.example.bluetoothgamepad.domain.CombinedHidDescriptor.bytes
    fun sdp(profile:GamepadProfile) = BluetoothHidDeviceAppSdpSettings(
        "Touch Gamepad & Mouse", "Android Bluetooth HID gamepad and Android TV touchpad", "BluetoothGamepad",
        BluetoothHidDevice.SUBCLASS1_COMBO, combinedBytes
    )
}
