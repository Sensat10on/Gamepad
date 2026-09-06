package com.example.bluetoothgamepad.input

/** Ownership table used by controls so every pointer updates/releases only what it captured. */
class TouchRouter {
    private val pointers = mutableMapOf<Long, String>()
    fun capture(pointerId: Long, controlId: String): Boolean =
        if (pointers.containsKey(pointerId)) false else { pointers[pointerId] = controlId; true }
    fun owner(pointerId: Long): String? = pointers[pointerId]
    fun release(pointerId: Long): String? = pointers.remove(pointerId)
    fun cancelAll(): Set<String> = pointers.values.toSet().also { pointers.clear() }
}
