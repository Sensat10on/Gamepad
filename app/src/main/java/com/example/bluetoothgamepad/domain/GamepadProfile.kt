package com.example.bluetoothgamepad.domain

interface GamepadProfile {
    val id: String
    val displayName: String
    val reportId: Int get() = 1
    val reportDescriptor: ByteArray get() = HidReportEncoder.REPORT_DESCRIPTOR
    fun encode(state: GamepadState): ByteArray = HidReportEncoder.encode(state)
}

/**
 * Selectable controller types.
 *
 * Only [MOUSE] changes the over-the-air report ID; [ANDROID] keeps the same report but reorders
 * the button bits for the Linux kernel's positional mapping; [EIGHT_BIT] and [SEGA] only change
 * the on-screen control set. [GAMEPAD] is the standard four-axis / two-trigger / hat / 16-button
 * layout in the physical-gamepad button order.
 */
enum class ProfileKind(val profile: GamepadProfile) {
    GAMEPAD(StandardGamepadProfile), ANDROID(AndroidGamepadProfile),
    EIGHT_BIT(EightBitProfile), SEGA(SegaStyleProfile), MOUSE(MouseProfile);

    /** Localized label for the settings dropdown; [GamepadProfile.displayName] stays language-neutral. */
    fun title(language: String): String = when (this) {
        GAMEPAD -> when (language) { "ru" -> "Стандартный геймпад"; "uk" -> "Стандартний геймпад"; else -> profile.displayName }
        ANDROID -> when (language) { "ru" -> "Android-геймпад"; "uk" -> "Android-геймпад"; else -> profile.displayName }
        SEGA -> when (language) { "ru", "uk" -> "Sega, 6 кнопок"; else -> profile.displayName }
        MOUSE -> when (language) { "ru" -> "Мышь / тачпад"; "uk" -> "Мишка / тачпад"; else -> profile.displayName }
        EIGHT_BIT -> profile.displayName
    }

    companion object {
        /**
         * The removed ids "generic", "xbox" and "playstation" are unknown now and fall through to
         * [GAMEPAD] — which is exactly the behaviour they produced before the merge, so settings
         * saved by older versions keep working without a migration step.
         */
        fun fromId(id: String) = entries.firstOrNull { it.profile.id == id } ?: GAMEPAD
    }
}
