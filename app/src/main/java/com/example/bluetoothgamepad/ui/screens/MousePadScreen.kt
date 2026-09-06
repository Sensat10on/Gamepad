package com.example.bluetoothgamepad.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import com.example.bluetoothgamepad.data.GamepadSettings
import com.example.bluetoothgamepad.domain.KeyboardReport
import com.example.bluetoothgamepad.R
import kotlin.math.roundToInt
import android.os.SystemClock

@Composable fun MousePadScreen(settings:GamepadSettings,onMove:(Int,Int,Int)->Unit,onClick:(Int)->Unit,onKey:(Int)->Unit){
    val control=Color(settings.controlColor.toInt())
    val label=Color(settings.buttonLabelColor.toInt())
    val language=settings.language
    val hint=when(language){"uk"->"Ведіть пальцем для руху курсора · двічі торкніться для кліку";"en"->"Slide to move the pointer · double-tap to click";else->"Ведите пальцем для движения курсора · дважды коснитесь для клика"}
    val left=when(language){"uk"->"Ліва кнопка";"en"->"Left button";else->"Левая кнопка"}
    val right=when(language){"uk"->"Права кнопка";"en"->"Right button";else->"Правая кнопка"}
    val landscape=LocalConfiguration.current.orientation==Configuration.ORIENTATION_LANDSCAPE
    if(landscape) Column(Modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
        Row(Modifier.weight(1f).fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(20.dp),verticalAlignment=Alignment.CenterVertically){
            TouchPadSurface(Modifier.weight(1f).fillMaxHeight(),settings,control,label,hint,onMove,onClick)
            ScrollStrip(Modifier.width(108.dp).fillMaxHeight(),settings,control,label,onMove)
            TvNavigationPad(control,label,onKey)
        }
        MouseButtons(left,right,control,label,onClick)
    } else Column(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
        Row(Modifier.weight(1f).fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly,verticalAlignment=Alignment.CenterVertically){
            ScrollStrip(Modifier.width(76.dp).fillMaxHeight(),settings,control,label,onMove)
            TvNavigationPad(control,label,onKey)
        }
        TouchPadSurface(Modifier.weight(1f).fillMaxWidth(),settings,control,label,hint,onMove,onClick)
        MouseButtons(left,right,control,label,onClick)
    }
}

@Composable private fun TouchPadSurface(modifier:Modifier,settings:GamepadSettings,control:Color,label:Color,hint:String,onMove:(Int,Int,Int)->Unit,onClick:(Int)->Unit){
    var lastTapAt by remember{mutableLongStateOf(0L)}
    Box(modifier.background(control.copy(alpha=.32f),RoundedCornerShape(28.dp)).border(2.dp,control,RoundedCornerShape(28.dp)).pointerInput(settings.touchpadSpeed,settings.touchpadDoubleTapMs){
        awaitEachGesture {
            val down=awaitFirstDown();var moved=false
            do {val event=awaitPointerEvent();val change=event.changes.firstOrNull()?:break
                if(change.pressed&&change.id==down.id){val delta=change.positionChange();if(delta.getDistance()>.7f){moved=true;val x=(delta.x*settings.touchpadSpeed).roundToInt().coerceIn(-8,8);val y=(delta.y*settings.touchpadSpeed).roundToInt().coerceIn(-8,8);if(x!=0||y!=0)onMove(x,y,0)};change.consume()}
            }while(change.pressed)
            if(!moved){val now=SystemClock.uptimeMillis();if(now-lastTapAt<=settings.touchpadDoubleTapMs){onClick(1);lastTapAt=0}else lastTapAt=now}
        }
    },contentAlignment=Alignment.Center){Text(hint,color=label,style=MaterialTheme.typography.titleMedium)}
}

@Composable private fun ScrollStrip(modifier:Modifier,settings:GamepadSettings,control:Color,label:Color,onMove:(Int,Int,Int)->Unit){
    Box(modifier.background(control.copy(alpha=.32f),RoundedCornerShape(28.dp)).border(2.dp,control,RoundedCornerShape(28.dp)).pointerInput(settings.touchpadScrollLines){detectVerticalDragGestures{change,amount->change.consume();val wheel=((-amount/10f).roundToInt()*settings.touchpadScrollLines).coerceIn(-24,24);if(wheel!=0)onMove(0,0,wheel)}},contentAlignment=Alignment.Center){Text("↕\nSCROLL",color=label)}
}

@Composable private fun MouseButtons(left:String,right:String,control:Color,label:Color,onClick:(Int)->Unit){
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)){
        Button({onClick(1)},Modifier.weight(1f),colors=ButtonDefaults.buttonColors(containerColor=control,contentColor=label)){Text(left)}
        Button({onClick(2)},Modifier.weight(1f),colors=ButtonDefaults.buttonColors(containerColor=control,contentColor=label)){Text(right)}
    }
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
