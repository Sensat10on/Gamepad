package com.example.bluetoothgamepad.domain

/**
 * Same descriptor and axes as [StandardGamepadProfile], but with the button indices the Linux
 * kernel expects from a HID Game Pad collection.
 *
 * The kernel maps buttons positionally, so on Android the standard (SDL/Xbox) order makes Select
 * and Start arrive as L1/R1 and the stick clicks as Select/Start. This profile places every
 * control on the index that turns into the matching `BUTTON_*` key code.
 *
 * Pick it when the host is Android or Android TV; pick [StandardGamepadProfile] for Windows and
 * other DirectInput hosts.
 */
object AndroidGamepadProfile : GamepadProfile {
    override val id = "android"
    override val displayName = "Android gamepad"
    override fun encode(state: GamepadState): ByteArray = HidReportEncoder.encodeAndroid(state)
}
