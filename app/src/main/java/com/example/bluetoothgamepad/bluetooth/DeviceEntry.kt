package com.example.bluetoothgamepad.bluetooth

/**
 * A Bluetooth device prepared for display.
 *
 * The UI never touches [android.bluetooth.BluetoothDevice] itself: every accessor that needs
 * `BLUETOOTH_CONNECT` is evaluated here, where the permission check and the failure handling live.
 * Reading `name`/`address`/`bondState` from a composable crashed with SecurityException when the
 * user revoked the permission while the device list was still on screen.
 */
data class DeviceEntry(val address: String, val label: String, val bonded: Boolean)
