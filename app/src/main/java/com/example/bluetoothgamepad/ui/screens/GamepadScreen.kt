package com.example.bluetoothgamepad.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.bluetoothgamepad.data.GamepadSettings
import com.example.bluetoothgamepad.domain.*
import com.example.bluetoothgamepad.ui.controls.*

@Composable fun GamepadScreen(profile:ProfileKind,settings:GamepadSettings,update:((GamepadState)->GamepadState)->Unit) {
    val controlColor=Color(settings.controlColor.toInt())
    val labelColor=Color(settings.buttonLabelColor.toInt());val dpadLineColor=Color(settings.dpadLineColor.toInt())
    BoxWithConstraints(Modifier.fillMaxSize()){
        val tablet=maxWidth>=840.dp
        if(tablet){
            when(profile){
                ProfileKind.EIGHT_BIT -> TabletEightBitLayout(maxWidth,maxHeight,controlColor,labelColor,dpadLineColor,update)
                ProfileKind.SEGA -> TabletSegaLayout(maxWidth,maxHeight,controlColor,labelColor,dpadLineColor,update)
                else -> TabletModernLayout(maxWidth,maxHeight,profile,settings,controlColor,labelColor,dpadLineColor,update)
            }
        }else when(profile){
            ProfileKind.EIGHT_BIT -> EightBitLayout(controlColor,labelColor,dpadLineColor,update)
            ProfileKind.SEGA -> SegaLayout(controlColor,labelColor,dpadLineColor,update)
            else -> ModernLayout(profile,settings,controlColor,labelColor,dpadLineColor,update)
        }
    }
}

@Composable private fun TabletModernLayout(w:androidx.compose.ui.unit.Dp,h:androidx.compose.ui.unit.Dp,profile:ProfileKind,settings:GamepadSettings,controlColor:Color,labelColor:Color,lineColor:Color,update:((GamepadState)->GamepadState)->Unit){
    Box(Modifier.fillMaxSize()){
        Box(Modifier.offset(w*.25f,h*.01f).graphicsLayer(scaleX=1.15f,scaleY=1.15f)){TriggerControl("L1",controlColor=controlColor){v->update{it.copy(l1=v>0)}}}
        Row(Modifier.offset(w*.40f,h*.01f),horizontalArrangement=Arrangement.spacedBy(10.dp)){SmallOvalButton("View",controlColor){v->update{it.copy(select=v)}};SmallOvalButton("Home",controlColor){v->update{it.copy(home=v)}};SmallOvalButton("Menu",controlColor){v->update{it.copy(start=v)}}}
        Box(Modifier.offset(w*.68f,h*.01f).graphicsLayer(scaleX=1.15f,scaleY=1.15f)){TriggerControl("R1",controlColor=controlColor){v->update{it.copy(r1=v>0)}}}
        Box(Modifier.offset(w*.03f,h*.10f).graphicsLayer(scaleX=1.25f,scaleY=1.25f,rotationZ=-28f)){TriggerControl("L2",TriggerDirection.INCREASE_LEFT,settings.triggerStep,controlColor){v->update{it.copy(leftTrigger=v)}}}
        Box(Modifier.offset(w*.82f,h*.10f).graphicsLayer(scaleX=1.25f,scaleY=1.25f,rotationZ=28f)){TriggerControl("R2",TriggerDirection.INCREASE_RIGHT,settings.triggerStep,controlColor){v->update{it.copy(rightTrigger=v)}}}
        Box(Modifier.offset(w*.06f,h*.39f).graphicsLayer(scaleX=1.45f,scaleY=1.45f)){DPad(controlColor,lineColor,onDirection={d->update{it.copy(dpad=d)}})}
        Box(Modifier.offset(w*.28f,h*.59f).graphicsLayer(scaleX=1.45f,scaleY=1.45f)){AnalogStick(settings.stickDeadZone,settings.stickSensitivity,settings.invertLeftY,settings.stickHoldDelayMs,controlColor,onStickPressed={p->update{it.copy(l3=p)}},onValue={x,y->update{it.copy(leftX=x,leftY=y)}})}
        Box(Modifier.offset(w*.58f,h*.38f).graphicsLayer(scaleX=1.35f,scaleY=1.35f)){FaceCluster(profile,controlColor,labelColor,update)}
        Box(Modifier.offset(w*.82f,h*.62f).graphicsLayer(scaleX=1.45f,scaleY=1.45f)){AnalogStick(settings.stickDeadZone,settings.stickSensitivity,settings.invertRightY,settings.stickHoldDelayMs,controlColor,onStickPressed={p->update{it.copy(r3=p)}},onValue={x,y->update{it.copy(rightX=x,rightY=y)}})}
    }
}

@Composable private fun TabletEightBitLayout(w:androidx.compose.ui.unit.Dp,h:androidx.compose.ui.unit.Dp,controlColor:Color,labelColor:Color,lineColor:Color,update:((GamepadState)->GamepadState)->Unit){
    Box(Modifier.fillMaxSize()){
        Box(Modifier.offset(w*.17f,h*.43f).graphicsLayer(scaleX=1.55f,scaleY=1.55f)){DPad(controlColor,lineColor,onDirection={d->update{it.copy(dpad=d)}})}
        Box(Modifier.offset(w*.48f,h*.40f)){SmallOvalButton("Home",controlColor){v->update{it.copy(home=v)}}}
        Box(Modifier.offset(w*.42f,h*.65f)){SmallOvalButton("SELECT",controlColor){v->update{it.copy(select=v)}}};Box(Modifier.offset(w*.55f,h*.65f)){SmallOvalButton("START",controlColor){v->update{it.copy(start=v)}}}
        Box(Modifier.offset(w*.76f,h*.44f).graphicsLayer(scaleX=1.45f,scaleY=1.45f)){FaceCluster(ProfileKind.EIGHT_BIT,controlColor,labelColor,update)}
    }
}

@Composable private fun TabletSegaLayout(w:androidx.compose.ui.unit.Dp,h:androidx.compose.ui.unit.Dp,controlColor:Color,labelColor:Color,lineColor:Color,update:((GamepadState)->GamepadState)->Unit){
    Box(Modifier.fillMaxSize()){
        Box(Modifier.offset(w*.15f,h*.38f).graphicsLayer(scaleX=1.55f,scaleY=1.55f)){DPad(controlColor,lineColor,onDirection={d->update{it.copy(dpad=d)}})}
        Box(Modifier.offset(w*.38f,h*.57f)){SmallOvalButton("SELECT",controlColor){v->update{it.copy(select=v)}}};Box(Modifier.offset(w*.49f,h*.57f)){SmallOvalButton("START",controlColor){v->update{it.copy(start=v)}}};Box(Modifier.offset(w*.44f,h*.86f)){SmallOvalButton("HOME",controlColor){v->update{it.copy(home=v)}}}
        Box(Modifier.offset(w*.64f,h*.36f).graphicsLayer(scaleX=1.35f,scaleY=1.35f)){Box(Modifier.size(230.dp,155.dp)){
            Box(Modifier.offset(0.dp,24.dp)){SegaButton("X",controlColor,labelColor){v->update{it.copy(north=v)}}};Box(Modifier.offset(72.dp,12.dp)){SegaButton("Y",controlColor,labelColor){v->update{it.copy(l1=v)}}};Box(Modifier.offset(144.dp,0.dp)){SegaButton("Z",controlColor,labelColor){v->update{it.copy(r1=v)}}}
            Box(Modifier.offset(10.dp,90.dp)){SegaButton("A",controlColor,labelColor){v->update{it.copy(south=v)}}};Box(Modifier.offset(82.dp,78.dp)){SegaButton("B",controlColor,labelColor){v->update{it.copy(east=v)}}};Box(Modifier.offset(154.dp,66.dp)){SegaButton("C",controlColor,labelColor){v->update{it.copy(west=v)}}}
        }}
    }
}

@Composable private fun ModernLayout(profile:ProfileKind,settings:GamepadSettings,controlColor:Color,labelColor:Color,dpadLineColor:Color,update:((GamepadState)->GamepadState)->Unit){
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=12.dp,vertical=8.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.Center,verticalAlignment=Alignment.CenterVertically){
                TriggerControl("L1",controlColor=controlColor){v->update{it.copy(l1=v>0)}};Spacer(Modifier.width(12.dp))
                SmallOvalButton("View",controlColor){v->update{it.copy(select=v)}};Spacer(Modifier.width(7.dp));SmallOvalButton("Home",controlColor){v->update{it.copy(home=v)}};Spacer(Modifier.width(7.dp));SmallOvalButton("Menu",controlColor){v->update{it.copy(start=v)}}
                Spacer(Modifier.width(12.dp));TriggerControl("R1",controlColor=controlColor){v->update{it.copy(r1=v>0)}}
            }
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
                TriggerControl("L2",TriggerDirection.INCREASE_LEFT,settings.triggerStep,controlColor){v->update{it.copy(leftTrigger=v)}}
                TriggerControl("R2",TriggerDirection.INCREASE_RIGHT,settings.triggerStep,controlColor){v->update{it.copy(rightTrigger=v)}}
            }
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){
                DPad(controlColor,dpadLineColor,onDirection={d->update{it.copy(dpad=d)}})
                AnalogStick(settings.stickDeadZone,settings.stickSensitivity,settings.invertLeftY,settings.stickHoldDelayMs,controlColor,onStickPressed={pressed->update{it.copy(l3=pressed)}},onValue={x,y->update{it.copy(leftX=x,leftY=y)}})
                Box(Modifier.size(145.dp)){
                    FaceButton(FacePosition.NORTH,profile,controlColor,labelColor,{v->update{it.copy(north=v)}},Modifier.align(Alignment.TopCenter))
                    FaceButton(FacePosition.WEST,profile,controlColor,labelColor,{v->update{it.copy(west=v)}},Modifier.align(Alignment.CenterStart))
                    FaceButton(FacePosition.EAST,profile,controlColor,labelColor,{v->update{it.copy(east=v)}},Modifier.align(Alignment.CenterEnd))
                    FaceButton(FacePosition.SOUTH,profile,controlColor,labelColor,{v->update{it.copy(south=v)}},Modifier.align(Alignment.BottomCenter))
                }
                AnalogStick(settings.stickDeadZone,settings.stickSensitivity,settings.invertRightY,settings.stickHoldDelayMs,controlColor,onStickPressed={pressed->update{it.copy(r3=pressed)}},onValue={x,y->update{it.copy(rightX=x,rightY=y)}})
            }
        }
}

@Composable private fun EightBitLayout(controlColor:Color,labelColor:Color,dpadLineColor:Color,update:((GamepadState)->GamepadState)->Unit){
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),verticalArrangement=Arrangement.spacedBy(18.dp)){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.Center){SmallOvalButton("Home",controlColor){v->update{it.copy(home=v)}}}
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceAround){
            DPad(controlColor,dpadLineColor,onDirection={d->update{it.copy(dpad=d)}})
            Row(horizontalArrangement=Arrangement.spacedBy(44.dp)){SmallOvalButton("SELECT",controlColor){v->update{it.copy(select=v)}};SmallOvalButton("START",controlColor){v->update{it.copy(start=v)}}}
            FaceCluster(ProfileKind.EIGHT_BIT,controlColor,labelColor,update)
        }
    }
}

@Composable private fun SegaLayout(controlColor:Color,labelColor:Color,dpadLineColor:Color,update:((GamepadState)->GamepadState)->Unit){
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),verticalArrangement=Arrangement.spacedBy(18.dp)){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.Center){SmallOvalButton("HOME",controlColor){v->update{it.copy(home=v)}}}
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceAround){
            DPad(controlColor,dpadLineColor,onDirection={d->update{it.copy(dpad=d)}})
            Row(horizontalArrangement=Arrangement.spacedBy(44.dp)){SmallOvalButton("SELECT",controlColor){v->update{it.copy(select=v)}};SmallOvalButton("START",controlColor){v->update{it.copy(start=v)}}}
            Box(Modifier.size(230.dp,155.dp)){
                Box(Modifier.offset(0.dp,24.dp)){SegaButton("X",controlColor,labelColor){v->update{it.copy(north=v)}}}
                Box(Modifier.offset(72.dp,12.dp)){SegaButton("Y",controlColor,labelColor){v->update{it.copy(l1=v)}}}
                Box(Modifier.offset(144.dp,0.dp)){SegaButton("Z",controlColor,labelColor){v->update{it.copy(r1=v)}}}
                Box(Modifier.offset(10.dp,90.dp)){SegaButton("A",controlColor,labelColor){v->update{it.copy(south=v)}}}
                Box(Modifier.offset(82.dp,78.dp)){SegaButton("B",controlColor,labelColor){v->update{it.copy(east=v)}}}
                Box(Modifier.offset(154.dp,66.dp)){SegaButton("C",controlColor,labelColor){v->update{it.copy(west=v)}}}
            }
        }
    }
}

@Composable private fun FaceCluster(profile:ProfileKind,controlColor:Color,labelColor:Color,update:((GamepadState)->GamepadState)->Unit){
    Box(Modifier.size(145.dp)){
        FaceButton(FacePosition.NORTH,profile,controlColor,labelColor,{v->update{it.copy(north=v)}},Modifier.align(Alignment.TopCenter))
        FaceButton(FacePosition.WEST,profile,controlColor,labelColor,{v->update{it.copy(west=v)}},Modifier.align(Alignment.CenterStart))
        FaceButton(FacePosition.EAST,profile,controlColor,labelColor,{v->update{it.copy(east=v)}},Modifier.align(Alignment.CenterEnd))
        FaceButton(FacePosition.SOUTH,profile,controlColor,labelColor,{v->update{it.copy(south=v)}},Modifier.align(Alignment.BottomCenter))
    }
}
