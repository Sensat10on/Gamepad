package com.example.bluetoothgamepad.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.settingsStore by preferencesDataStore("gamepad_settings")

data class GamepadSettings(
    val selectedProfile: String = "gamepad", val lastHostAddress: String? = null,
    val stickDeadZone: Float = .12f, val stickSensitivity: Float = 1f,
    val invertLeftY: Boolean = false, val invertRightY: Boolean = false,
    val hapticsEnabled: Boolean = true, val darkTheme: Boolean = true,
    val language: String = "ru", val triggerStep: Float = .05f,
    val stickHoldDelayMs: Int = 500, val backgroundUri: String? = null,
    val controlColor: Long = 0xFF6750A4L, val wallpaperZoom: Float = 1f,
    val wallpaperOffsetX: Float = 0f, val wallpaperOffsetY: Float = 0f,
    val automaticTextColor: Boolean = true, val textColor: Long = 0xFFFFFFFFL,
    val backgroundColor: Long = 0xFF121016L, val buttonLabelColor: Long = 0xFFFFFFFFL,
    val dpadLineColor: Long = 0xFFD7D0E3L,
    val touchpadSpeed: Float = 1.35f, val touchpadScrollLines: Int = 3, val touchpadDoubleTapMs: Int = 300,
    val landscapeLocked: Boolean = false,
    /** Draws the controller shell between the wallpaper and the controls. */
    val showPadBody: Boolean = true,
    /** Optional image clipped into the controller shell; independent of [backgroundUri]. */
    val padImageUri: String? = null,
    /** Draws ○ □ ✕ △ instead of A B X Y on the face buttons. Cosmetic only. */
    val playStationGlyphs: Boolean = false
)

class SettingsRepository(private val context: Context) {
    private object Keys {
        val profile = stringPreferencesKey("selected_profile"); val host = stringPreferencesKey("last_host_address")
        val deadZone = floatPreferencesKey("stick_dead_zone"); val sensitivity = floatPreferencesKey("stick_sensitivity")
        val invertLeftY = booleanPreferencesKey("invert_left_y"); val invertRightY = booleanPreferencesKey("invert_right_y")
        val haptics = booleanPreferencesKey("haptics_enabled"); val dark = booleanPreferencesKey("dark_theme")
        val language = stringPreferencesKey("language")
        val triggerStep = floatPreferencesKey("trigger_step")
        val stickHoldDelay = intPreferencesKey("stick_hold_delay_ms")
        val backgroundUri = stringPreferencesKey("background_uri")
        val controlColor = longPreferencesKey("control_color")
        val wallpaperZoom = floatPreferencesKey("wallpaper_zoom")
        val wallpaperOffsetX = floatPreferencesKey("wallpaper_offset_x")
        val wallpaperOffsetY = floatPreferencesKey("wallpaper_offset_y")
        val automaticTextColor = booleanPreferencesKey("automatic_text_color")
        val textColor = longPreferencesKey("text_color")
        val backgroundColor = longPreferencesKey("background_color")
        val buttonLabelColor = longPreferencesKey("button_label_color")
        val dpadLineColor = longPreferencesKey("dpad_line_color")
        val touchpadSpeed = floatPreferencesKey("touchpad_speed")
        val touchpadScrollLines = intPreferencesKey("touchpad_scroll_lines")
        val touchpadDoubleTapMs = intPreferencesKey("touchpad_double_tap_ms")
        val landscapeLocked = booleanPreferencesKey("landscape_locked")
        val playStationGlyphs = booleanPreferencesKey("play_station_glyphs")
        val showPadBody = booleanPreferencesKey("show_pad_body")
        val padImageUri = stringPreferencesKey("pad_image_uri")
    }
    val settings = context.settingsStore.data.map { p -> GamepadSettings(
        p[Keys.profile] ?: "gamepad", p[Keys.host], p[Keys.deadZone] ?: .12f, p[Keys.sensitivity] ?: 1f,
        p[Keys.invertLeftY] ?: false, p[Keys.invertRightY] ?: false, p[Keys.haptics] ?: true, p[Keys.dark] ?: true,
        p[Keys.language] ?: "ru", p[Keys.triggerStep] ?: .05f,
        p[Keys.stickHoldDelay] ?: 500, p[Keys.backgroundUri], p[Keys.controlColor] ?: 0xFF6750A4L,
        p[Keys.wallpaperZoom] ?: 1f, p[Keys.wallpaperOffsetX] ?: 0f, p[Keys.wallpaperOffsetY] ?: 0f,
        p[Keys.automaticTextColor] ?: true, p[Keys.textColor] ?: 0xFFFFFFFFL,
        p[Keys.backgroundColor] ?: 0xFF121016L, p[Keys.buttonLabelColor] ?: 0xFFFFFFFFL,
        p[Keys.dpadLineColor] ?: 0xFFD7D0E3L,
        p[Keys.touchpadSpeed] ?: 1.35f, p[Keys.touchpadScrollLines] ?: 3, p[Keys.touchpadDoubleTapMs] ?: 300,
        p[Keys.landscapeLocked] ?: false,
        p[Keys.showPadBody] ?: true, p[Keys.padImageUri],
        p[Keys.playStationGlyphs] ?: false
    ) }
    suspend fun setProfile(value: String) = context.settingsStore.edit { it[Keys.profile] = value }
    suspend fun setLastHost(value: String) = context.settingsStore.edit { it[Keys.host] = value }
    suspend fun update(deadZone: Float, sensitivity: Float, invertLeftY: Boolean, invertRightY: Boolean, haptics: Boolean, dark: Boolean, triggerStep: Float, stickHoldDelayMs: Int, touchpadSpeed:Float, touchpadScrollLines:Int, touchpadDoubleTapMs:Int, landscapeLocked:Boolean) =
        context.settingsStore.edit { p -> p[Keys.deadZone]=deadZone; p[Keys.sensitivity]=sensitivity; p[Keys.invertLeftY]=invertLeftY; p[Keys.invertRightY]=invertRightY; p[Keys.haptics]=haptics; p[Keys.dark]=dark;p[Keys.triggerStep]=triggerStep;p[Keys.stickHoldDelay]=stickHoldDelayMs;p[Keys.touchpadSpeed]=touchpadSpeed;p[Keys.touchpadScrollLines]=touchpadScrollLines;p[Keys.touchpadDoubleTapMs]=touchpadDoubleTapMs;p[Keys.landscapeLocked]=landscapeLocked }
    suspend fun setLanguage(value: String) = context.settingsStore.edit { it[Keys.language] = value }

    /** Persists every appearance field of [value]; the caller passes the merged draft. */
    suspend fun setAppearance(value: GamepadSettings) = context.settingsStore.edit { p ->
        if(value.backgroundUri==null)p.remove(Keys.backgroundUri) else p[Keys.backgroundUri]=value.backgroundUri
        p[Keys.controlColor]=value.controlColor
        p[Keys.wallpaperZoom]=value.wallpaperZoom;p[Keys.wallpaperOffsetX]=value.wallpaperOffsetX;p[Keys.wallpaperOffsetY]=value.wallpaperOffsetY
        p[Keys.automaticTextColor]=value.automaticTextColor;p[Keys.textColor]=value.textColor
        p[Keys.backgroundColor]=value.backgroundColor
        p[Keys.dark]=value.darkTheme
        p[Keys.buttonLabelColor]=value.buttonLabelColor;p[Keys.dpadLineColor]=value.dpadLineColor
        p[Keys.playStationGlyphs]=value.playStationGlyphs
        p[Keys.showPadBody]=value.showPadBody
        if(value.padImageUri==null)p.remove(Keys.padImageUri) else p[Keys.padImageUri]=value.padImageUri
    }
}
