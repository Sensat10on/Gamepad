package com.example.bluetoothgamepad.bluetooth

import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import com.example.bluetoothgamepad.domain.CombinedHidDescriptor

object HidDescriptor {
    /**
     * Every profile shares one standards-based descriptor (gamepad + relative mouse + keyboard),
     * so the SDP record does not change when the user switches layout.
     */
    fun sdp() = BluetoothHidDeviceAppSdpSettings(
        "Touch Gamepad & Mouse", "Android Bluetooth HID gamepad and Android TV touchpad", "BluetoothGamepad",
        BluetoothHidDevice.SUBCLASS1_COMBO, CombinedHidDescriptor.bytes
    )
}
