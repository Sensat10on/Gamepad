package com.example.bluetoothgamepad.domain

object MouseProfile : GamepadProfile {
    override val id = "mouse"
    override val displayName = "Mouse / touchpad"
    override val reportId = 2
    override val reportDescriptor = byteArrayOf(
        0x05,0x01, 0x09,0x02, 0xA1.toByte(),0x01, 0x85.toByte(),0x02,
        0x09,0x01, 0xA1.toByte(),0x00,
        0x05,0x09, 0x19,0x01, 0x29,0x03, 0x15,0x00, 0x25,0x01,
        0x75,0x01, 0x95.toByte(),0x03, 0x81.toByte(),0x02,
        0x75,0x05, 0x95.toByte(),0x01, 0x81.toByte(),0x03,
        // Report-protocol mouse; a report ID and wheel do not implement boot protocol.
        0x05,0x01, 0x09,0x30, 0x09,0x31,
        0x15,0x81.toByte(), 0x25,0x7F, 0x75,0x08, 0x95.toByte(),0x02, 0x81.toByte(),0x06,
        0x09,0x38, 0x15,0x81.toByte(), 0x25,0x7F, 0x75,0x08, 0x95.toByte(),0x01, 0x81.toByte(),0x06,
        0xC0.toByte(), 0xC0.toByte()
    )
    override fun encode(state:GamepadState)=byteArrayOf(0,0,0,0)
    /**
     * Relative motion is clamped to ±64 instead of ±16 so the "pointer speed" setting has room to
     * work; the descriptor allows -127..127, so this stays well inside the logical range.
     */
    fun encodeMouse(buttons:Int,dx:Int,dy:Int,wheel:Int)=byteArrayOf(
        (buttons and 7).toByte(),dx.coerceIn(-64,64).toByte(),dy.coerceIn(-64,64).toByte(),wheel.coerceIn(-24,24).toByte()
    )
}
