package com.example.bluetoothgamepad.domain

import kotlin.math.roundToInt

object HidReportEncoder {
    /*
     * Universal gamepad descriptor, Report ID 1:
     * 4 signed-looking (0..255, center 128) stick axes; 2 unsigned triggers;
     * one 4-bit hat plus padding; 16 one-bit buttons. Payload is 9 bytes.
     * 05 01 Generic Desktop, 09 05 Game Pad, A1 01 Application, 85 01 Report ID.
     *
     * The axes already line up with what Android's keylayouts expect:
     *   X/Y -> left stick, Z -> left trigger, Rx/Ry -> right stick, Rz -> right trigger,
     *   Hat switch -> HAT_X/HAT_Y. Only the button indices need a host-specific variant.
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

    /*
     * Control order used by both bit tables below:
     *   south, east, west, north, l1, r1, select, start, l3, r3, home
     */
    private fun pressedFlags(s: GamepadState) = booleanArrayOf(
        s.south, s.east, s.west, s.north, s.l1, s.r1, s.select, s.start, s.l3, s.r3, s.home
    )

    /**
     * Physical-gamepad order (SDL / Xbox 360), which Windows and most host software assume:
     * A B X Y, L1 R1, Select Start, L3 R3, Home.
     */
    private val STANDARD_BITS = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

    /**
     * Order the Linux kernel produces for a HID Game Pad collection. `hidinput_configure_usage()`
     * maps buttons positionally (`BTN_GAMEPAD + n - 1`), so on Android only these indices become
     * the BUTTON_* codes the labels promise:
     *
     *   1 A, 2 B, 4 X, 5 Y, 7 L1, 8 R1, 11 Select, 12 Start, 13 Mode, 14 L3, 15 R3
     *
     * 3 (BTN_C), 6 (BTN_Z), 9/10 (BTN_TL2/TR2) and 16 are intentionally left unused.
     */
    private val ANDROID_BITS = intArrayOf(0, 1, 3, 4, 6, 7, 10, 11, 13, 14, 12)

    /** Windows / generic host layout. */
    fun encode(s: GamepadState): ByteArray = encode(s, STANDARD_BITS)

    /** Android (Linux kernel) layout. */
    fun encodeAndroid(s: GamepadState): ByteArray = encode(s, ANDROID_BITS)

    private fun encode(s: GamepadState, bits: IntArray): ByteArray {
        val pressed = pressedFlags(s)
        var buttons = 0
        for (i in bits.indices) if (pressed[i]) buttons = buttons or (1 shl bits[i])
        return byteArrayOf(
            axis(s.leftX), axis(s.leftY), axis(s.rightX), axis(s.rightY),
            trigger(s.leftTrigger), trigger(s.rightTrigger), s.dpad.hatValue.toByte(),
            buttons.toByte(), (buttons ushr 8).toByte()
        )
    }

    internal fun axis(value: Float) = (((value.coerceIn(-1f,1f) + 1f) * 127.5f).roundToInt()).toByte()
    internal fun trigger(value: Float) = (value.coerceIn(0f,1f) * 255f).roundToInt().toByte()
}
