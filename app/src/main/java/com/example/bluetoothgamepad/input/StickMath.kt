package com.example.bluetoothgamepad.input

import kotlin.math.hypot

data class StickVector(val x: Float, val y: Float)

object StickMath {
    fun normalize(dx: Float, dy: Float, radius: Float, deadZone: Float = 0.12f, sensitivity: Float = 1f): StickVector {
        if (radius <= 0f) return StickVector(0f, 0f)
        var x = dx / radius
        var y = dy / radius
        val rawMagnitude = hypot(x, y)
        if (rawMagnitude > 1f) { x /= rawMagnitude; y /= rawMagnitude }
        val magnitude = hypot(x, y)
        val dz = deadZone.coerceIn(0f, 0.95f)
        if (magnitude <= dz) return StickVector(0f, 0f)
        val scaled = (((magnitude - dz) / (1f - dz)) * sensitivity).coerceIn(0f, 1f)
        return StickVector(x / magnitude * scaled, y / magnitude * scaled)
    }
}
