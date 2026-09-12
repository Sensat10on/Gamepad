package com.example.bluetoothgamepad.domain

import org.junit.Assert.*
import org.junit.Test

class HidReportEncoderTest {
    @Test fun neutralReport(){assertArrayEquals(byteArrayOf(128.toByte(),128.toByte(),128.toByte(),128.toByte(),0,0,8,0,0),HidReportEncoder.encode(GamepadState()))}
    @Test fun axesAndTriggersUseFullRange(){val r=HidReportEncoder.encode(GamepadState(leftX=-1f,leftY=1f,leftTrigger=1f));assertEquals(0,r[0].toInt());assertEquals(255,r[1].toInt() and 255);assertEquals(255,r[4].toInt() and 255)}
    @Test fun buttonsArePackedAcrossBytes(){val r=HidReportEncoder.encode(GamepadState(south=true,north=true,r3=true,select=true,start=true,home=true));assertEquals(0b11001001,r[7].toInt() and 255);assertEquals(0b00000110,r[8].toInt() and 255)}

    /**
     * Hosts address gamepad buttons positionally, so each control must land on the index a
     * physical pad uses: A B X Y LB RB Select Start L3 R3 Home. Moving the menu buttons after
     * the bumpers is the point of this test.
     */
    @Test fun buttonsFollowThePhysicalGamepadOrder(){
        fun pressedBits(state:GamepadState):Int{
            val r=HidReportEncoder.encode(state)
            return (r[7].toInt() and 255) or ((r[8].toInt() and 255) shl 8)
        }
        assertEquals(1 shl 0,pressedBits(GamepadState(south=true)))
        assertEquals(1 shl 1,pressedBits(GamepadState(east=true)))
        assertEquals(1 shl 2,pressedBits(GamepadState(west=true)))
        assertEquals(1 shl 3,pressedBits(GamepadState(north=true)))
        assertEquals(1 shl 4,pressedBits(GamepadState(l1=true)))
        assertEquals(1 shl 5,pressedBits(GamepadState(r1=true)))
        assertEquals(1 shl 6,pressedBits(GamepadState(select=true)))
        assertEquals(1 shl 7,pressedBits(GamepadState(start=true)))
        assertEquals(1 shl 8,pressedBits(GamepadState(l3=true)))
        assertEquals(1 shl 9,pressedBits(GamepadState(r3=true)))
        assertEquals(1 shl 10,pressedBits(GamepadState(home=true)))
    }
    @Test fun hatEncodingMatchesDirections(){DPadDirection.entries.forEach{assertEquals(it.hatValue,HidReportEncoder.encode(GamepadState(dpad=it))[6].toInt())}}
    @Test fun mouseReportEncodesButtonsMotionAndWheel(){assertArrayEquals(byteArrayOf(2,64,-64,5),MouseProfile.encodeMouse(2,400,-300,5))}
    /** The speed setting needs headroom: a ±16 clamp saturated immediately. */
    @Test fun mouseMotionClampAllowsTheSpeedSettingToMatter(){
        assertEquals(64,MouseProfile.encodeMouse(0,1000,0,0)[1].toInt())
        assertEquals(32,MouseProfile.encodeMouse(0,32,0,0)[1].toInt())
    }
    @Test fun keyboardReportPlacesAndroidTvKeyInFirstSlot(){assertArrayEquals(byteArrayOf(0,0,KeyboardReport.ENTER.toByte(),0,0,0,0,0),KeyboardReport.encode(KeyboardReport.ENTER))}

    /**
     * Every gamepad layout is the same thing on the wire. Only their on-screen control set differs,
     * which is why "Generic", "Xbox-style" and "PlayStation-style" were merged into one profile.
     */
    @Test fun everyGamepadProfileUsesTheUniversalDescriptorAndPayload(){
        val state=GamepadState(east=true,dpad=DPadDirection.WEST)
        listOf(EightBitProfile,SegaStyleProfile).forEach{other->
            assertEquals(1,other.reportId)
            assertArrayEquals(StandardGamepadProfile.reportDescriptor,other.reportDescriptor)
            assertArrayEquals(StandardGamepadProfile.encode(state),other.encode(state))
        }
    }

    @Test fun onlyTheMouseProfileDivergesFromTheGamepadReport(){
        assertEquals(2,MouseProfile.reportId)
        assertFalse(MouseProfile.reportDescriptor.contentEquals(StandardGamepadProfile.reportDescriptor))
    }

    /**
     * The Android profile uses the indices the Linux kernel assigns positionally inside a
     * HID Game Pad collection: 1 A, 2 B, 4 X, 5 Y, 7 L1, 8 R1, 11 Select, 12 Start, 13 Mode,
     * 14 L3, 15 R3. Anything else would arrive as BUTTON_C / BUTTON_Z / BUTTON_L2 on Android.
     */
    @Test fun androidProfileUsesTheKernelButtonOrder(){
        fun bits(state:GamepadState):Int{
            val r=AndroidGamepadProfile.encode(state)
            return (r[7].toInt() and 255) or ((r[8].toInt() and 255) shl 8)
        }
        assertEquals(1 shl 0,bits(GamepadState(south=true)))
        assertEquals(1 shl 1,bits(GamepadState(east=true)))
        assertEquals(1 shl 3,bits(GamepadState(west=true)))
        assertEquals(1 shl 4,bits(GamepadState(north=true)))
        assertEquals(1 shl 6,bits(GamepadState(l1=true)))
        assertEquals(1 shl 7,bits(GamepadState(r1=true)))
        assertEquals(1 shl 10,bits(GamepadState(select=true)))
        assertEquals(1 shl 11,bits(GamepadState(start=true)))
        assertEquals(1 shl 12,bits(GamepadState(home=true)))
        assertEquals(1 shl 13,bits(GamepadState(l3=true)))
        assertEquals(1 shl 14,bits(GamepadState(r3=true)))
    }

    /** Both gamepad profiles share the descriptor, the report id and the whole axis payload. */
    @Test fun androidProfileDiffersOnlyInButtonBits(){
        val s=GamepadState(leftX=1f,rightY=-1f,dpad=DPadDirection.NORTH,leftTrigger=.5f,rightTrigger=1f)
        val standard=StandardGamepadProfile.encode(s)
        val android=AndroidGamepadProfile.encode(s)
        assertEquals(1,AndroidGamepadProfile.reportId)
        assertArrayEquals(StandardGamepadProfile.reportDescriptor,AndroidGamepadProfile.reportDescriptor)
        assertArrayEquals(standard.copyOfRange(0,7),android.copyOfRange(0,7))
    }

    @Test fun androidProfileIsSelectableAndMigratesFromNothing(){
        assertEquals(ProfileKind.ANDROID,ProfileKind.fromId("android"))
        assertEquals(ProfileKind.GAMEPAD,ProfileKind.fromId("generic"))
    }

    @Test fun removedProfileIdsResolveToTheStandardGamepad(){
        listOf("generic","xbox","playstation").forEach{assertEquals(ProfileKind.GAMEPAD,ProfileKind.fromId(it))}
        assertEquals(ProfileKind.MOUSE,ProfileKind.fromId("mouse"))
        assertEquals(ProfileKind.EIGHT_BIT,ProfileKind.fromId("8bit"))
        assertEquals(ProfileKind.SEGA,ProfileKind.fromId("sega"))
        assertEquals(ProfileKind.GAMEPAD,ProfileKind.fromId("unknown-id"))
    }
}
