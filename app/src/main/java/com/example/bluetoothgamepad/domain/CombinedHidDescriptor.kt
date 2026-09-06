package com.example.bluetoothgamepad.domain

object CombinedHidDescriptor {
    // End Collection does not reset HID global state. Isolate each function,
    // particularly the gamepad hat's physical range (0..315).
    val bytes: ByteArray = listOf(
        HidReportEncoder.REPORT_DESCRIPTOR, MouseProfile.reportDescriptor, KeyboardReport.descriptor
    ).fold(byteArrayOf()) { result, descriptor ->
        result + byteArrayOf(0xA4.toByte()) + descriptor + byteArrayOf(0xB4.toByte())
    }
}
