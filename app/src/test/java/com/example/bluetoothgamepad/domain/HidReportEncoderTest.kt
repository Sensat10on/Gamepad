package com.example.bluetoothgamepad.domain

import org.junit.Assert.*
import org.junit.Test

class HidReportEncoderTest {
    @Test fun neutralReport(){assertArrayEquals(byteArrayOf(128.toByte(),128.toByte(),128.toByte(),128.toByte(),0,0,8,0,0),HidReportEncoder.encode(GamepadState()))}
    @Test fun axesAndTriggersUseFullRange(){val r=HidReportEncoder.encode(GamepadState(leftX=-1f,leftY=1f,leftTrigger=1f));assertEquals(0,r[0].toInt());assertEquals(255,r[1].toInt() and 255);assertEquals(255,r[4].toInt() and 255)}
    @Test fun buttonsArePackedAcrossBytes(){val r=HidReportEncoder.encode(GamepadState(south=true,north=true,r3=true,select=true,start=true,home=true));assertEquals(0b10001001,r[7].toInt() and 255);assertEquals(0b00000111,r[8].toInt() and 255)}
    @Test fun hatEncodingMatchesDirections(){DPadDirection.entries.forEach{assertEquals(it.hatValue,HidReportEncoder.encode(GamepadState(dpad=it))[6].toInt())}}
    @Test fun profilesShareDescriptorAndPayload(){val s=GamepadState(east=true,dpad=DPadDirection.WEST);assertArrayEquals(GenericHidProfile.encode(s),XboxStyleProfile.encode(s));assertArrayEquals(GenericHidProfile.reportDescriptor,PlayStationStyleProfile.reportDescriptor)}
    @Test fun retroProfilesUseUniversalDescriptor(){assertArrayEquals(GenericHidProfile.reportDescriptor,EightBitProfile.reportDescriptor);assertArrayEquals(GenericHidProfile.reportDescriptor,SegaStyleProfile.reportDescriptor)}
    @Test fun mouseReportEncodesButtonsMotionAndWheel(){assertArrayEquals(byteArrayOf(2,16,-16,5),MouseProfile.encodeMouse(2,400,-300,5))}
    @Test fun keyboardReportPlacesAndroidTvKeyInFirstSlot(){assertArrayEquals(byteArrayOf(0,0,KeyboardReport.ENTER.toByte(),0,0,0,0,0),KeyboardReport.encode(KeyboardReport.ENTER))}
}
