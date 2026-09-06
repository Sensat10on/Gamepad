package com.example.bluetoothgamepad.domain

import kotlin.math.roundToInt

object HidReportEncoder {
    /*
     * Universal gamepad descriptor, Report ID 1:
     * 4 signed-looking (0..255, center 128) stick axes; 2 unsigned triggers;
     * one 4-bit hat plus padding; 16 one-bit buttons. Payload is 9 bytes.
     * 05 01 Generic Desktop, 09 05 Game Pad, A1 01 Application, 85 01 Report ID.
     */
    val REPORT_DESCRIPTOR = byteArrayOf(
        0x05,0x01, 0x09,0x05, 0xA1.toByte(),0x01, 0x85.toByte(),0x01,
        0x09,0x30, 0x09,0x31, 0x09,0x33, 0x09,0x34,
        0x15,0x00, 0x26,0xFF.toByte(),0x00, 0x75,0x08, 0x95.toByte(),0x04, 0x81.toByte(),0x02,
        0x09,0x32, 0x09,0x35, 0x15,0x00, 0x26,0xFF.toByte(),0x00,
        0x75,0x08, 0x95.toByte(),0x02, 0x81.toByte(),0x02,
        0x05,0x01, 0x09,0x39, 0x15,0x00, 0x25,0x07, 0x35,0x00, 0x46,0x3B,0x01,
        0x65,0x14, 0x75,0x04, 0x95.toByte(),0x01, 0x81.toByte(),0x42,
        0x65,0x00, 0x75,0x04, 0x95.toByte(),0x01, 0x81.toByte(),0x03,
        0x05,0x09, 0x19,0x01, 0x29,0x10, 0x15,0x00, 0x25,0x01,
        0x75,0x01, 0x95.toByte(),0x10, 0x81.toByte(),0x02, 0xC0.toByte()
    )

    fun encode(s: GamepadState): ByteArray {
        var buttons = 0
        listOf(s.south,s.east,s.west,s.north,s.l1,s.r1,s.l3,s.r3,s.select,s.start,s.home)
            .forEachIndexed { i, pressed -> if (pressed) buttons = buttons or (1 shl i) }
        return byteArrayOf(
            axis(s.leftX), axis(s.leftY), axis(s.rightX), axis(s.rightY),
            trigger(s.leftTrigger), trigger(s.rightTrigger), s.dpad.hatValue.toByte(),
            buttons.toByte(), (buttons ushr 8).toByte()
        )
    }
    internal fun axis(value: Float) = (((value.coerceIn(-1f,1f) + 1f) * 127.5f).roundToInt()).toByte()
    internal fun trigger(value: Float) = (value.coerceIn(0f,1f) * 255f).roundToInt().toByte()
}
