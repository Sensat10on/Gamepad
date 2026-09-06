package com.example.bluetoothgamepad.domain

enum class DPadDirection(val hatValue: Int) {
    NORTH(0), NORTH_EAST(1), EAST(2), SOUTH_EAST(3), SOUTH(4),
    SOUTH_WEST(5), WEST(6), NORTH_WEST(7), CENTER(8)
}
