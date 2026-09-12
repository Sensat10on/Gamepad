package com.example.bluetoothgamepad.ui.controls

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.unit.dp
import com.example.bluetoothgamepad.input.TriggerMath
import com.example.bluetoothgamepad.ui.skins.LocalPadSkin
import com.example.bluetoothgamepad.ui.skins.darken
import com.example.bluetoothgamepad.ui.skins.lighten

enum class TriggerDirection { INCREASE_LEFT, INCREASE_RIGHT }

@Composable fun TriggerControl(label:String,direction:TriggerDirection?=null,step:Float=.05f,controlColor:Color=Color(0xFF6750A4),labelColor:Color=Color.White,onValue:(Float)->Unit) {
    val skin=LocalPadSkin.current
    var value by remember { mutableFloatStateOf(0f) }
    val width=164.dp
    Box(Modifier.size(width,44.dp).pointerInput(direction,step){
        awaitEachGesture {
            val down=awaitFirstDown()
            fun valueAt(x:Float)=if(direction==null)1f else TriggerMath.quantize(if(direction==TriggerDirection.INCREASE_LEFT)1f-x/size.width else x/size.width,step)
            value=valueAt(down.position.x); onValue(value)
            // finally: a cancelled gesture must not leave a trigger stuck at a non-zero value.
            try {
                while(true) {
                    val event=awaitPointerEvent();val change=event.changes.firstOrNull{it.id==down.id}?:break;if(!change.pressed)break
                    value=valueAt(change.position.x); onValue(value);change.consume()
                }
            } finally {
                value=0f;onValue(0f)
            }
        }
    },contentAlignment=Alignment.Center){
        Canvas(Modifier.matchParentSize()){
            val radius=size.height/2f
            val outline=Path().apply{addRoundRect(androidx.compose.ui.geometry.RoundRect(Rect(Offset.Zero,size),CornerRadius(radius,radius)))}
            drawPath(outline,darken(controlColor,.58f))
            if(value>0f){
                val fillWidth=size.width*value
                clipPath(outline){
                    val left=if(direction==TriggerDirection.INCREASE_LEFT) 0f else size.width-fillWidth
                    drawRect(
                        brush=Brush.verticalGradient(listOf(lighten(controlColor,.28f),controlColor,darken(controlColor,.30f))),
                        topLeft=Offset(left,0f),
                        size=Size(fillWidth,size.height)
                    )
                }
            }
            drawPath(outline,labelColor.copy(alpha=.28f),style=Stroke(width=size.height*.05f))
            if(skin.gloss) clipPath(outline){
                drawRect(
                    brush=Brush.verticalGradient(listOf(Color.White.copy(alpha=.16f),Color.Transparent),startY=0f,endY=size.height*.55f)
                )
            }
        }
        // L2/R2 are labelled with an arrow showing which way to slide, plus the current level.
        Text(
            when(direction){
                null->label
                TriggerDirection.INCREASE_LEFT->"◀  ${(value*100).toInt()}%"
                TriggerDirection.INCREASE_RIGHT->"${(value*100).toInt()}%  ▶"
            },
            color=labelColor
        )
    }
}
