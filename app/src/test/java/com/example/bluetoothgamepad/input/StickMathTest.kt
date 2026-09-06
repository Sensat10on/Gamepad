package com.example.bluetoothgamepad.input

import org.junit.Assert.assertEquals
import org.junit.Test

class StickMathTest {
    @Test fun centerIsNeutral() { val v=StickMath.normalize(0f,0f,100f);assertEquals(0f,v.x,0.0001f);assertEquals(0f,v.y,0.0001f) }
    @Test fun vectorIsClampedToCircle() { val v=StickMath.normalize(200f,0f,100f,0f);assertEquals(1f,v.x,0.0001f);assertEquals(0f,v.y,0.0001f) }
    @Test fun diagonalIsNormalized() { val v=StickMath.normalize(100f,100f,100f,0f);assertEquals(.7071f,v.x,.001f);assertEquals(.7071f,v.y,.001f) }
    @Test fun deadZoneIsRemovedAndRescaled() { assertEquals(0f,StickMath.normalize(10f,0f,100f,.2f).x,0f);assertEquals(.5f,StickMath.normalize(60f,0f,100f,.2f).x,.001f) }
}
