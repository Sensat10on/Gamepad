package io.github.sensat10on.gamepad.domain

/**
 * The single standards-based gamepad profile.
 *
 * "Generic HID", "Xbox-style" and "PlayStation-style" used to be three separate entries, but they
 * emitted byte-identical HID reports and rendered the same layout: the only difference was the
 * dropdown label and, for PlayStation, the face-button glyphs. The SDP record registered with the
 * Bluetooth stack does not depend on the profile at all (`HidDescriptor.sdp()` uses the combined
 * descriptor), so the choice never reached the host.
 *
 * The face glyphs are now an appearance option
 * ([io.github.sensat10on.gamepad.data.GamepadSettings.playStationGlyphs]) instead of a fake profile.
 */
object StandardGamepadProfile : GamepadProfile {
    override val id = "gamepad"
    override val displayName = "Gamepad"
}
