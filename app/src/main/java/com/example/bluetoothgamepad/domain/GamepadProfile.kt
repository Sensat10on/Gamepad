package com.example.bluetoothgamepad.domain

interface GamepadProfile {
    val id: String
    val displayName: String
    val reportId: Int get() = 1
    val reportDescriptor: ByteArray get() = HidReportEncoder.REPORT_DESCRIPTOR
    fun encode(state: GamepadState): ByteArray = HidReportEncoder.encode(state)
}

enum class ProfileKind(val profile: GamepadProfile) {
    GENERIC(GenericHidProfile), XBOX(XboxStyleProfile), PLAYSTATION(PlayStationStyleProfile),
    EIGHT_BIT(EightBitProfile), SEGA(SegaStyleProfile), MOUSE(MouseProfile);
    companion object { fun fromId(id: String) = entries.firstOrNull { it.profile.id == id } ?: GENERIC }
}
