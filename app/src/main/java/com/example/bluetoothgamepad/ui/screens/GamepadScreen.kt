package com.example.bluetoothgamepad.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.bluetoothgamepad.data.GamepadSettings
import com.example.bluetoothgamepad.domain.*
import com.example.bluetoothgamepad.ui.controls.*
import com.example.bluetoothgamepad.ui.skins.LocalPadSkin
import com.example.bluetoothgamepad.ui.skins.PadBody
import com.example.bluetoothgamepad.ui.skins.PadSkin
import com.example.bluetoothgamepad.ui.skins.padBodyColor

/**
 * Chooses a layout from the available window size rather than from the device class, because the
 * activity is not guaranteed to stay in landscape (Android 16 ignores fixed orientation on large
 * screens, and the user can unlock rotation).
 */
@Composable fun GamepadScreen(profile:ProfileKind,settings:GamepadSettings,update:((GamepadState)->GamepadState)->Unit) {
    val controlColor=Color(settings.controlColor.toInt())
    val labelColor=Color(settings.buttonLabelColor.toInt());val dpadLineColor=Color(settings.dpadLineColor.toInt())
    val padSkin=PadSkin.forProfile(profile)
    Box(Modifier.fillMaxSize()){
        // Layer 1 is the wallpaper behind everything, layer 2 is the controller shell (which can
        // carry its own image), layer 3 is the controls.
        if(settings.showPadBody){
            PadBody(
                skin=padSkin,
                bodyColor=padBodyColor(controlColor,settings.darkTheme),
                outlineColor=labelColor.copy(alpha=.20f),
                imageUri=settings.padImageUri,
                modifier=Modifier.fillMaxSize()
            )
        }
        CompositionLocalProvider(LocalPadSkin provides padSkin){
            BoxWithConstraints(Modifier.fillMaxSize().padding(if(settings.showPadBody) 14.dp else 0.dp)){
                val wide = maxWidth >= 840.dp
                val singleRow = maxWidth >= 700.dp
                when {
                    wide -> when(profile){
                        ProfileKind.EIGHT_BIT -> TabletEightBitLayout(maxWidth,maxHeight,settings,controlColor,labelColor,dpadLineColor,update)
                        ProfileKind.SEGA -> TabletSegaLayout(maxWidth,maxHeight,controlColor,labelColor,dpadLineColor,update)
                        else -> TabletModernLayout(maxWidth,maxHeight,settings,controlColor,labelColor,dpadLineColor,update)
                    }
                    singleRow -> when(profile){
                        ProfileKind.EIGHT_BIT -> EightBitLayout(settings,controlColor,labelColor,dpadLineColor,update)
                        ProfileKind.SEGA -> SegaLayout(controlColor,labelColor,dpadLineColor,update)
                        else -> ModernLayout(settings,controlColor,labelColor,dpadLineColor,update)
                    }
                    else -> when(profile){
                        ProfileKind.EIGHT_BIT -> CompactEightBitLayout(settings,controlColor,labelColor,dpadLineColor,update)
                        ProfileKind.SEGA -> CompactSegaLayout(controlColor,labelColor,dpadLineColor,update)
                        else -> CompactModernLayout(settings,controlColor,labelColor,dpadLineColor,update)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- compact (phone in portrait)

@Composable private fun CompactModernLayout(settings:GamepadSettings,controlColor:Color,labelColor:Color,lineColor:Color,update:((GamepadState)->GamepadState)->Unit){
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=10.dp,vertical=6.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
        // Portrait/book layout: the menu buttons sit above the bumpers, and the bumpers are the
        // same size as the triggers below them.
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.Center,verticalAlignment=Alignment.CenterVertically){
            SmallOvalButton("Select",controlColor,labelColor){v->update{it.copy(select=v)}}
            Spacer(Modifier.width(10.dp))
            SmallOvalButton("Home",controlColor,labelColor){v->update{it.copy(home=v)}}
            Spacer(Modifier.width(10.dp))
            SmallOvalButton("Start",controlColor,labelColor){v->update{it.copy(start=v)}}
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
            TriggerControl("L1",controlColor=controlColor,labelColor=labelColor){v->update{it.copy(l1=v>0)}}
            TriggerControl("R1",controlColor=controlColor,labelColor=labelColor){v->update{it.copy(r1=v>0)}}
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
            TriggerControl("L2",TriggerDirection.INCREASE_LEFT,settings.triggerStep,controlColor,labelColor){v->update{it.copy(leftTrigger=v)}}
            TriggerControl("R2",TriggerDirection.INCREASE_RIGHT,settings.triggerStep,controlColor,labelColor){v->update{it.copy(rightTrigger=v)}}
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly,verticalAlignment=Alignment.CenterVertically){
            DPad(controlColor,onDirection={d->update{it.copy(dpad=d)}},lineColor=lineColor)
            AnalogStick(settings.stickDeadZone,settings.stickSensitivity,settings.invertLeftY,settings.stickHoldDelayMs,controlColor,onStickPressed={p->update{it.copy(l3=p)}},onValue={x,y->update{it.copy(leftX=x,leftY=y)}})
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly,verticalAlignment=Alignment.CenterVertically){
            AnalogStick(settings.stickDeadZone,settings.stickSensitivity,settings.invertRightY,settings.stickHoldDelayMs,controlColor,onStickPressed={p->update{it.copy(r3=p)}},onValue={x,y->update{it.copy(rightX=x,rightY=y)}})
            FaceCluster(settings.playStationGlyphs,controlColor,labelColor,update)
        }
    }
}

@Composable private fun CompactEightBitLayout(settings:GamepadSettings,controlColor:Color,labelColor:Color,lineColor:Color,update:((GamepadState)->GamepadState)->Unit){
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=10.dp,vertical=8.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.Center){SmallOvalButton("Home",controlColor,labelColor){v->update{it.copy(home=v)}}}
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly,verticalAlignment=Alignment.CenterVertically){
            DPad(controlColor,onDirection={d->update{it.copy(dpad=d)}},lineColor=lineColor)
            FaceCluster(settings.playStationGlyphs,controlColor,labelColor,update)
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly){
            SmallOvalButton("Select",controlColor,labelColor){v->update{it.copy(select=v)}}
            SmallOvalButton("Start",controlColor,labelColor){v->update{it.copy(start=v)}}
        }
    }
}

@Composable private fun CompactSegaLayout(controlColor:Color,labelColor:Color,lineColor:Color,update:((GamepadState)->GamepadState)->Unit){
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=10.dp,vertical=8.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly,verticalAlignment=Alignment.CenterVertically){
            DPad(controlColor,onDirection={d->update{it.copy(dpad=d)}},lineColor=lineColor)
            SmallOvalButton("Home",controlColor,labelColor){v->update{it.copy(home=v)}}
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.Center){SegaCluster(controlColor,labelColor,update)}
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly){
            SmallOvalButton("Select",controlColor,labelColor){v->update{it.copy(select=v)}}
            SmallOvalButton("Start",controlColor,labelColor){v->update{it.copy(start=v)}}
        }
    }
}

// ---------------------------------------------------------------- tablet (>= 840dp wide)

@Composable private fun TabletModernLayout(w:Dp,h:Dp,settings:GamepadSettings,controlColor:Color,labelColor:Color,lineColor:Color,update:((GamepadState)->GamepadState)->Unit){
    Box(Modifier.fillMaxSize()){
        Box(Modifier.offset(w*.25f,h*.01f).graphicsLayer(scaleX=1.15f,scaleY=1.15f)){TriggerControl("L1",controlColor=controlColor,labelColor=labelColor){v->update{it.copy(l1=v>0)}}}
        Row(Modifier.offset(w*.40f,h*.01f),horizontalArrangement=Arrangement.spacedBy(10.dp)){SmallOvalButton("Select",controlColor,labelColor){v->update{it.copy(select=v)}};SmallOvalButton("Home",controlColor,labelColor){v->update{it.copy(home=v)}};SmallOvalButton("Start",controlColor,labelColor){v->update{it.copy(start=v)}}}
        Box(Modifier.offset(w*.68f,h*.01f).graphicsLayer(scaleX=1.15f,scaleY=1.15f)){TriggerControl("R1",controlColor=controlColor,labelColor=labelColor){v->update{it.copy(r1=v>0)}}}
        Box(Modifier.offset(w*.03f,h*.10f).graphicsLayer(scaleX=1.25f,scaleY=1.25f,rotationZ=-28f)){TriggerControl("L2",TriggerDirection.INCREASE_LEFT,settings.triggerStep,controlColor,labelColor){v->update{it.copy(leftTrigger=v)}}}
        Box(Modifier.offset(w*.82f,h*.10f).graphicsLayer(scaleX=1.25f,scaleY=1.25f,rotationZ=28f)){TriggerControl("R2",TriggerDirection.INCREASE_RIGHT,settings.triggerStep,controlColor,labelColor){v->update{it.copy(rightTrigger=v)}}}
        Box(Modifier.offset(w*.06f,h*.39f).graphicsLayer(scaleX=1.45f,scaleY=1.45f)){DPad(controlColor,onDirection={d->update{it.copy(dpad=d)}},lineColor=lineColor)}
        Box(Modifier.offset(w*.28f,h*.59f).graphicsLayer(scaleX=1.45f,scaleY=1.45f)){AnalogStick(settings.stickDeadZone,settings.stickSensitivity,settings.invertLeftY,settings.stickHoldDelayMs,controlColor,onStickPressed={p->update{it.copy(l3=p)}},onValue={x,y->update{it.copy(leftX=x,leftY=y)}})}
        Box(Modifier.offset(w*.58f,h*.38f).graphicsLayer(scaleX=1.45f,scaleY=1.45f)){AnalogStick(settings.stickDeadZone,settings.stickSensitivity,settings.invertRightY,settings.stickHoldDelayMs,controlColor,onStickPressed={p->update{it.copy(r3=p)}},onValue={x,y->update{it.copy(rightX=x,rightY=y)}})}
        Box(Modifier.offset(w*.78f,h*.60f).graphicsLayer(scaleX=1.35f,scaleY=1.35f)){FaceCluster(settings.playStationGlyphs,controlColor,labelColor,update)}
    }
}

@Composable private fun TabletEightBitLayout(w:Dp,h:Dp,settings:GamepadSettings,controlColor:Color,labelColor:Color,lineColor:Color,update:((GamepadState)->GamepadState)->Unit){
    Box(Modifier.fillMaxSize()){
        Box(Modifier.offset(w*.17f,h*.43f).graphicsLayer(scaleX=1.55f,scaleY=1.55f)){DPad(controlColor,onDirection={d->update{it.copy(dpad=d)}},lineColor=lineColor)}
        Box(Modifier.offset(w*.48f,h*.40f)){SmallOvalButton("Home",controlColor,labelColor){v->update{it.copy(home=v)}}}
        Box(Modifier.offset(w*.42f,h*.65f)){SmallOvalButton("Select",controlColor,labelColor){v->update{it.copy(select=v)}}};Box(Modifier.offset(w*.55f,h*.65f)){SmallOvalButton("Start",controlColor,labelColor){v->update{it.copy(start=v)}}}
        Box(Modifier.offset(w*.76f,h*.44f).graphicsLayer(scaleX=1.45f,scaleY=1.45f)){FaceCluster(settings.playStationGlyphs,controlColor,labelColor,update)}
    }
}

@Composable private fun TabletSegaLayout(w:Dp,h:Dp,controlColor:Color,labelColor:Color,lineColor:Color,update:((GamepadState)->GamepadState)->Unit){
    Box(Modifier.fillMaxSize()){
        Box(Modifier.offset(w*.15f,h*.38f).graphicsLayer(scaleX=1.55f,scaleY=1.55f)){DPad(controlColor,onDirection={d->update{it.copy(dpad=d)}},lineColor=lineColor)}
        Box(Modifier.offset(w*.38f,h*.57f)){SmallOvalButton("Select",controlColor,labelColor){v->update{it.copy(select=v)}}};Box(Modifier.offset(w*.49f,h*.57f)){SmallOvalButton("Start",controlColor,labelColor){v->update{it.copy(start=v)}}};Box(Modifier.offset(w*.44f,h*.86f)){SmallOvalButton("Home",controlColor,labelColor){v->update{it.copy(home=v)}}}
        Box(Modifier.offset(w*.64f,h*.36f).graphicsLayer(scaleX=1.35f,scaleY=1.35f)){SegaCluster(controlColor,labelColor,update)}
    }
}

// ---------------------------------------------------------------- single row (landscape phone)

@Composable private fun ModernLayout(settings:GamepadSettings,controlColor:Color,labelColor:Color,dpadLineColor:Color,update:((GamepadState)->GamepadState)->Unit){
        // Tight vertical budget: the top bar plus the padded shell leave little room in landscape,
        // and the scrollable column would otherwise clip the bottom of the face buttons.
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=12.dp,vertical=2.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.Center,verticalAlignment=Alignment.CenterVertically){
                TriggerControl("L1",controlColor=controlColor,labelColor=labelColor){v->update{it.copy(l1=v>0)}};Spacer(Modifier.width(12.dp))
                SmallOvalButton("Select",controlColor,labelColor){v->update{it.copy(select=v)}};Spacer(Modifier.width(7.dp));SmallOvalButton("Home",controlColor,labelColor){v->update{it.copy(home=v)}};Spacer(Modifier.width(7.dp));SmallOvalButton("Start",controlColor,labelColor){v->update{it.copy(start=v)}}
                Spacer(Modifier.width(12.dp));TriggerControl("R1",controlColor=controlColor,labelColor=labelColor){v->update{it.copy(r1=v>0)}}
            }
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
                TriggerControl("L2",TriggerDirection.INCREASE_LEFT,settings.triggerStep,controlColor,labelColor){v->update{it.copy(leftTrigger=v)}}
                TriggerControl("R2",TriggerDirection.INCREASE_RIGHT,settings.triggerStep,controlColor,labelColor){v->update{it.copy(rightTrigger=v)}}
            }
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){
                DPad(controlColor,onDirection={d->update{it.copy(dpad=d)}},lineColor=dpadLineColor)
                AnalogStick(settings.stickDeadZone,settings.stickSensitivity,settings.invertLeftY,settings.stickHoldDelayMs,controlColor,onStickPressed={pressed->update{it.copy(l3=pressed)}},onValue={x,y->update{it.copy(leftX=x,leftY=y)}})
                AnalogStick(settings.stickDeadZone,settings.stickSensitivity,settings.invertRightY,settings.stickHoldDelayMs,controlColor,onStickPressed={pressed->update{it.copy(r3=pressed)}},onValue={x,y->update{it.copy(rightX=x,rightY=y)}})
                FaceCluster(settings.playStationGlyphs,controlColor,labelColor,update)
            }
        }
}

@Composable private fun EightBitLayout(settings:GamepadSettings,controlColor:Color,labelColor:Color,dpadLineColor:Color,update:((GamepadState)->GamepadState)->Unit){
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),verticalArrangement=Arrangement.spacedBy(18.dp)){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.Center){SmallOvalButton("Home",controlColor,labelColor){v->update{it.copy(home=v)}}}
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceAround){
            DPad(controlColor,onDirection={d->update{it.copy(dpad=d)}},lineColor=dpadLineColor)
            Row(horizontalArrangement=Arrangement.spacedBy(44.dp)){SmallOvalButton("Select",controlColor,labelColor){v->update{it.copy(select=v)}};SmallOvalButton("Start",controlColor,labelColor){v->update{it.copy(start=v)}}}
            FaceCluster(settings.playStationGlyphs,controlColor,labelColor,update)
        }
    }
}

@Composable private fun SegaLayout(controlColor:Color,labelColor:Color,dpadLineColor:Color,update:((GamepadState)->GamepadState)->Unit){
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),verticalArrangement=Arrangement.spacedBy(18.dp)){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.Center){SmallOvalButton("Home",controlColor,labelColor){v->update{it.copy(home=v)}}}
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceAround){
            DPad(controlColor,onDirection={d->update{it.copy(dpad=d)}},lineColor=dpadLineColor)
            Row(horizontalArrangement=Arrangement.spacedBy(44.dp)){SmallOvalButton("Select",controlColor,labelColor){v->update{it.copy(select=v)}};SmallOvalButton("Start",controlColor,labelColor){v->update{it.copy(start=v)}}}
            SegaCluster(controlColor,labelColor,update)
        }
    }
}

// ---------------------------------------------------------------- shared clusters

@Composable private fun FaceCluster(playStationGlyphs:Boolean,controlColor:Color,labelColor:Color,update:((GamepadState)->GamepadState)->Unit){
    Box(Modifier.size(145.dp)){
        FaceButton(FacePosition.NORTH,playStationGlyphs,{v->update{it.copy(north=v)}},Modifier.align(Alignment.TopCenter),controlColor,labelColor)
        FaceButton(FacePosition.WEST,playStationGlyphs,{v->update{it.copy(west=v)}},Modifier.align(Alignment.CenterStart),controlColor,labelColor)
        FaceButton(FacePosition.EAST,playStationGlyphs,{v->update{it.copy(east=v)}},Modifier.align(Alignment.CenterEnd),controlColor,labelColor)
        FaceButton(FacePosition.SOUTH,playStationGlyphs,{v->update{it.copy(south=v)}},Modifier.align(Alignment.BottomCenter),controlColor,labelColor)
    }
}

@Composable private fun SegaCluster(controlColor:Color,labelColor:Color,update:((GamepadState)->GamepadState)->Unit){
    Box(Modifier.size(230.dp,155.dp)){
        Box(Modifier.offset(0.dp,24.dp)){SegaButton("X",controlColor,labelColor){v->update{it.copy(north=v)}}}
        Box(Modifier.offset(72.dp,12.dp)){SegaButton("Y",controlColor,labelColor){v->update{it.copy(l1=v)}}}
        Box(Modifier.offset(144.dp,0.dp)){SegaButton("Z",controlColor,labelColor){v->update{it.copy(r1=v)}}}
        Box(Modifier.offset(10.dp,90.dp)){SegaButton("A",controlColor,labelColor){v->update{it.copy(south=v)}}}
        Box(Modifier.offset(82.dp,78.dp)){SegaButton("B",controlColor,labelColor){v->update{it.copy(east=v)}}}
        Box(Modifier.offset(154.dp,66.dp)){SegaButton("C",controlColor,labelColor){v->update{it.copy(west=v)}}}
    }
}
