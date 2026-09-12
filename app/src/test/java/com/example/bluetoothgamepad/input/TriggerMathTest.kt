package com.example.bluetoothgamepad.input

import org.junit.Assert.assertEquals
import org.junit.Test

class TriggerMathTest {
    @Test fun snapsToNearestStep() {
        assertEquals(0.25f, TriggerMath.quantize(0.26f, 0.05f), 0.0001f)
        assertEquals(0.30f, TriggerMath.quantize(0.31f, 0.05f), 0.0001f)
        assertEquals(1.00f, TriggerMath.quantize(0.99f, 0.05f), 0.0001f)
    }

    @Test fun clampsIntoUnitRange() {
        assertEquals(0f, TriggerMath.quantize(-3f, 0.05f), 0f)
        assertEquals(1f, TriggerMath.quantize(7f, 0.05f), 0f)
    }

    @Test fun clampsTheStepItself() {
        // A step of zero must not divide by zero, and a huge step must not swallow the range.
        assertEquals(0.5f, TriggerMath.quantize(0.5f, 0f), 0.0001f)
        assertEquals(0.5f, TriggerMath.quantize(0.5f, 9f), 0.0001f)
        assertEquals(0.01f, TriggerMath.quantize(0.01f, 0.0001f), 0.00001f)
    }

    @Test fun neutralInputStaysNeutral() {
        assertEquals(0f, TriggerMath.quantize(0f, 0.05f), 0f)
    }
}
