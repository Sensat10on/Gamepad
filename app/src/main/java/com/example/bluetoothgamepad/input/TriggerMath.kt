package com.example.bluetoothgamepad.input

import kotlin.math.round

/**
 * Trigger quantisation lives here (not inside the composable) so it can be unit tested.
 */
object TriggerMath {
    const val MIN_STEP = 0.01f
    const val MAX_STEP = 0.25f

    /** Snaps [raw] to the nearest multiple of [step] and clamps the result into 0..1. */
    fun quantize(raw: Float, step: Float): Float {
        val safe = step.coerceIn(MIN_STEP, MAX_STEP)
        return (round(raw / safe) * safe).coerceIn(0f, 1f)
    }
}
