package com.example.bluetoothgamepad.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import android.os.SystemClock
import com.example.bluetoothgamepad.R
import com.example.bluetoothgamepad.data.GamepadSettings
import com.example.bluetoothgamepad.domain.KeyboardReport
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Largest per-event pointer delta handed to the HID layer. The old value was ±8, which saturated
 * almost immediately and made the "pointer speed" setting look dead; the encoder now allows ±64.
 */
private const val MAX_POINTER_DELTA = 32

/** Host-side click detection needs the button held for at least a few frames. */
private const val CLICK_HOLD_MS = 60L

@Composable fun MousePadScreen(settings:GamepadSettings,onMove:(Int,Int,Int)->Unit,onButton:(Int,Boolean)->Unit,onKey:(Int)->Unit){
    val control=Color(settings.controlColor.toInt())
    val label=Color(settings.buttonLabelColor.toInt())
    val language=settings.language
    val left=when(language){"uk"->"Ліва кнопка";"en"->"Left button";else->"Левая кнопка"}
    val right=when(language){"uk"->"Права кнопка";"en"->"Right button";else->"Правая кнопка"}
    val landscape=LocalConfiguration.current.orientation==Configuration.ORIENTATION_LANDSCAPE
    if(landscape) Column(Modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
        Row(Modifier.weight(1f).fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(20.dp),verticalAlignment=Alignment.CenterVertically){
            TouchPadSurface(Modifier.weight(1f).fillMaxHeight(),settings,control,onMove,onButton)
            ScrollStrip(Modifier.width(108.dp).fillMaxHeight(),settings,control,onMove)
            TvNavigationPad(control,label,onKey)
        }
        MouseButtons(left,right,control,label,onButton)
    } else Column(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
        Row(Modifier.weight(1f).fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly,verticalAlignment=Alignment.CenterVertically){
            ScrollStrip(Modifier.width(76.dp).fillMaxHeight(),settings,control,onMove)
            TvNavigationPad(control,label,onKey)
        }
        TouchPadSurface(Modifier.weight(1f).fillMaxWidth(),settings,control,onMove,onButton)
        MouseButtons(left,right,control,label,onButton)
    }
}

@Composable private fun TouchPadSurface(modifier:Modifier,settings:GamepadSettings,control:Color,onMove:(Int,Int,Int)->Unit,onButton:(Int,Boolean)->Unit){
    var lastTapAt by remember{mutableLongStateOf(0L)}
    val scope=rememberCoroutineScope()
    Box(modifier.background(control.copy(alpha=.32f),RoundedCornerShape(28.dp)).border(2.dp,control,RoundedCornerShape(28.dp)).pointerInput(settings.touchpadSpeed,settings.touchpadDoubleTapMs){
        awaitEachGesture {
            val down=awaitFirstDown();var moved=false
            do {val event=awaitPointerEvent();val change=event.changes.firstOrNull()?:break
                if(change.pressed&&change.id==down.id){val delta=change.positionChange();if(delta.getDistance()>.7f){moved=true;val x=(delta.x*settings.touchpadSpeed).roundToInt().coerceIn(-MAX_POINTER_DELTA,MAX_POINTER_DELTA);val y=(delta.y*settings.touchpadSpeed).roundToInt().coerceIn(-MAX_POINTER_DELTA,MAX_POINTER_DELTA);if(x!=0||y!=0)onMove(x,y,0)};change.consume()}
            }while(change.pressed)
            if(!moved){val now=SystemClock.uptimeMillis();if(now-lastTapAt<=settings.touchpadDoubleTapMs){
                lastTapAt=0
                // Hold the button briefly: a zero-length press is dropped by most hosts.
                scope.launch{onButton(1,true);delay(CLICK_HOLD_MS);onButton(1,false)}
            }else lastTapAt=now}
        }
    }){}
}

@Composable private fun ScrollStrip(modifier:Modifier,settings:GamepadSettings,control:Color,onMove:(Int,Int,Int)->Unit){
    Box(modifier.background(control.copy(alpha=.32f),RoundedCornerShape(28.dp)).border(2.dp,control,RoundedCornerShape(28.dp)).pointerInput(settings.touchpadScrollLines){detectVerticalDragGestures{change,amount->change.consume();val wheel=((-amount/10f).roundToInt()*settings.touchpadScrollLines).coerceIn(-24,24);if(wheel!=0)onMove(0,0,wheel)}}){}
}

@Composable private fun MouseButtons(left:String,right:String,control:Color,label:Color,onButton:(Int,Boolean)->Unit){
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)){
        MouseHoldButton(left,1,control,label,onButton,Modifier.weight(1f))
        MouseHoldButton(right,2,control,label,onButton,Modifier.weight(1f))
    }
}

/**
 * Press-and-hold mouse button. Holding it while dragging on the touchpad sends motion with the
 * button bit set, which is what a host needs for drag-and-drop.
 */
@Composable private fun MouseHoldButton(text:String,button:Int,control:Color,label:Color,onButton:(Int,Boolean)->Unit,modifier:Modifier){
    var pressed by remember{mutableStateOf(false)}
    Box(modifier.height(56.dp).background(if(pressed) control else control.copy(alpha=.72f),RoundedCornerShape(18.dp)).pointerInput(button){
        awaitEachGesture {
            val down=awaitFirstDown()
            pressed=true;onButton(button,true)
            try {
                while(true){val event=awaitPointerEvent();val change=event.changes.firstOrNull{it.id==down.id}?:break;if(!change.pressed)break;change.consume()}
            } finally {
                pressed=false;onButton(button,false)
            }
        }
    },contentAlignment=Alignment.Center){Text(text,color=label)}
}

@Composable private fun TvNavigationPad(control:Color,label:Color,onKey:(Int)->Unit){
    Box(Modifier.size(190.dp)){
        Image(painterResource(R.drawable.ic_nintendo_dpad),null,Modifier.matchParentSize())
        Box(Modifier.align(Alignment.TopCenter).size(64.dp).clickable{onKey(KeyboardReport.UP)})
        Box(Modifier.align(Alignment.CenterStart).size(64.dp).clickable{onKey(KeyboardReport.LEFT)})
        Box(Modifier.align(Alignment.CenterEnd).size(64.dp).clickable{onKey(KeyboardReport.RIGHT)})
        Box(Modifier.align(Alignment.BottomCenter).size(64.dp).clickable{onKey(KeyboardReport.DOWN)})
        Box(Modifier.align(Alignment.Center).size(62.dp).clickable{onKey(KeyboardReport.ENTER)})
    }
}
