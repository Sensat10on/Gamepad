package com.example.bluetoothgamepad.domain

import org.junit.Assert.*
import org.junit.Test

class CombinedHidDescriptorTest {
    @Test fun mouseInputsDoNotInheritGamepadPhysicalRange() {
        val globals=mutableMapOf<Int,Int>()
        val stack=java.util.ArrayDeque<Map<Int,Int>>()
        var offset=0
        var mouseBits=0
        var relativeFields=0
        val data=CombinedHidDescriptor.bytes
        while(offset<data.size){
            val prefix=data[offset++].toInt() and 255
            val count=when(prefix and 3){3->4;else->prefix and 3}
            var value=0
            repeat(count){value=value or ((data[offset++].toInt() and 255) shl (8*it))}
            val tag=prefix ushr 4
            when((prefix ushr 2) and 3){
                1->when(tag){
                    10->stack.push(globals.toMap())
                    11->{globals.clear();globals.putAll(stack.pop())}
                    else->globals[tag]=value
                }
                0->if(tag==8 && globals[8]==2){
                    assertEquals(0,globals[3]?:0)
                    assertEquals(0,globals[4]?:0)
                    assertEquals(0,globals[6]?:0)
                    mouseBits+=(globals[7]?:0)*(globals[9]?:0)
                    if(value and 1==0 && value and 4!=0){
                        assertEquals(0x81,globals[1]);assertEquals(127,globals[2])
                        relativeFields+=globals[9]?:0
                    }
                }
            }
        }
        assertTrue(stack.isEmpty())
        assertEquals(32,mouseBits)
        assertEquals(3,relativeFields)
    }
}
