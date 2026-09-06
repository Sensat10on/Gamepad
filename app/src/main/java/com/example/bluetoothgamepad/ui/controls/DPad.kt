package com.example.bluetoothgamepad.ui.controls

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.unit.dp
import com.example.bluetoothgamepad.domain.DPadDirection
import kotlin.math.abs

@Composable fun DPad(controlColor:Color,lineColor:Color=Color(0xFFD7D0E3),onDirection:(DPadDirection)->Unit, modifier:Modifier=Modifier) {
    var active by remember { mutableStateOf(DPadDirection.CENTER) }
    Canvas(modifier.size(150.dp).pointerInput(Unit) {
        awaitEachGesture {
            val down=awaitFirstDown(); val pointer=down.id
            do {
                val event=awaitPointerEvent(); val change=event.changes.firstOrNull{it.id==pointer} ?: break
                if(!change.pressed) break
                val dx=change.position.x-size.width/2f; val dy=change.position.y-size.height/2f
                val next=if(abs(dx)<size.width*.12f && abs(dy)<size.height*.12f) DPadDirection.CENTER else {
                    val diagonal=abs(dx)>abs(dy)*.45f && abs(dy)>abs(dx)*.45f
                    if(diagonal) when { dx>0&&dy<0->DPadDirection.NORTH_EAST;dx>0&&dy>0->DPadDirection.SOUTH_EAST;dx<0&&dy>0->DPadDirection.SOUTH_WEST;else->DPadDirection.NORTH_WEST }
                    else if(abs(dx)>abs(dy)) if(dx>0) DPadDirection.EAST else DPadDirection.WEST
                    else if(dy>0) DPadDirection.SOUTH else DPadDirection.NORTH
                }
                if(next!=active){active=next;onDirection(next)}; change.consume()
            }while(true)
            active=DPadDirection.CENTER;onDirection(DPadDirection.CENTER)
        }
    }) {
        val w=size.width; val h=size.height; val arm=w*.34f
        val path=Path().apply { moveTo(arm,0f);lineTo(w-arm,0f);lineTo(w-arm,h*.33f);lineTo(w,h*.33f);lineTo(w,h*.67f);lineTo(w-arm,h*.67f);lineTo(w-arm,h);lineTo(arm,h);lineTo(arm,h*.67f);lineTo(0f,h*.67f);lineTo(0f,h*.33f);lineTo(arm,h*.33f);close() }
        drawPath(path,if(active==DPadDirection.CENTER) controlColor.copy(alpha=.45f) else controlColor)
        drawPath(path,lineColor,style=Stroke(width=4f))
        val c=Offset(w/2,h/2); val arrow=lineColor
        drawLine(arrow,Offset(c.x,c.y-25f),Offset(c.x,c.y-48f),8f,StrokeCap.Round)
        drawLine(arrow,Offset(c.x,c.y+25f),Offset(c.x,c.y+48f),8f,StrokeCap.Round)
        drawLine(arrow,Offset(c.x-25f,c.y),Offset(c.x-48f,c.y),8f,StrokeCap.Round)
        drawLine(arrow,Offset(c.x+25f,c.y),Offset(c.x+48f,c.y),8f,StrokeCap.Round)
    }
}
