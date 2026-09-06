package com.example.bluetoothgamepad.domain

data class GamepadState(
    val leftX: Float = 0f, val leftY: Float = 0f,
    val rightX: Float = 0f, val rightY: Float = 0f,
    val leftTrigger: Float = 0f, val rightTrigger: Float = 0f,
    val dpad: DPadDirection = DPadDirection.CENTER,
    val south: Boolean = false, val east: Boolean = false,
    val west: Boolean = false, val north: Boolean = false,
    val l1: Boolean = false, val r1: Boolean = false,
    val l3: Boolean = false, val r3: Boolean = false,
    val start: Boolean = false, val select: Boolean = false, val home: Boolean = false
)
