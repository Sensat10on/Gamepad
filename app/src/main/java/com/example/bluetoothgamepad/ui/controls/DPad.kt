package com.example.bluetoothgamepad.ui.controls

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.unit.dp
import com.example.bluetoothgamepad.domain.DPadDirection
import com.example.bluetoothgamepad.ui.skins.LocalPadSkin
import com.example.bluetoothgamepad.ui.skins.darken
import com.example.bluetoothgamepad.ui.skins.lighten
import com.example.bluetoothgamepad.ui.skins.plusPath
import kotlin.math.abs

@Composable fun DPad(controlColor:Color,onDirection:(DPadDirection)->Unit, modifier:Modifier=Modifier,lineColor:Color=Color(0xFFD7D0E3)) {
    val skin=LocalPadSkin.current
    var active by remember { mutableStateOf(DPadDirection.CENTER) }
    Canvas(modifier.size(150.dp).pointerInput(Unit) {
        awaitEachGesture {
            val down=awaitFirstDown(); val pointer=down.id
            // finally: a cancelled gesture must still report CENTER so the host hat is released.
            try {
                while(true) {
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
                }
            } finally {
                active=DPadDirection.CENTER;onDirection(DPadDirection.CENTER)
            }
        }
    }) {
        val pressed=active!=DPadDirection.CENTER
        val center=Offset(size.width/2f,size.height/2f)
        val minDim=size.minDimension

        // Classic cross: one merged path, bevelled with a vertical gradient so the arms read as
        // moulded plastic instead of a flat silhouette. No socket circle behind it.
        val cross=plusPath(Size(size.width,size.height),thickness=.35f,corner=minDim*.10f)
        if(skin.gloss){
            drawPath(cross,Brush.verticalGradient(
                listOf(lighten(controlColor,.34f),controlColor,darken(controlColor,.34f)),
                startY=0f,endY=size.height))
        }else{
            drawPath(cross,controlColor)
        }
        drawPath(cross,darken(controlColor,.62f),style=Stroke(width=minDim*.030f))

        // Direction marks: small arrows on the arm tips instead of the previous screw-like dots.
        val mark=if(pressed) lighten(controlColor,.55f) else darken(controlColor,.38f)
        val tip=minDim*.375f
        val side=minDim*.050f
        fun arrow(tx:Float,ty:Float,nx:Float,ny:Float){
            val path=Path().apply{
                moveTo(tx,ty)
                lineTo(tx-nx*side+ny*side,ty-ny*side-nx*side)
                lineTo(tx-nx*side-ny*side,ty-ny*side+nx*side)
                close()
            }
            drawPath(path,mark)
        }
        arrow(center.x,center.y-tip,0f,-1f)
        arrow(center.x,center.y+tip,0f,1f)
        arrow(center.x-tip,center.y,-1f,0f)
        arrow(center.x+tip,center.y,1f,0f)
        drawCircle(darken(controlColor,.30f),minDim*.045f,center)
    }
}
