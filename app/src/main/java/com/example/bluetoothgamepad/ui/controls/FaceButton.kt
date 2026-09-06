package com.example.bluetoothgamepad.ui.controls

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.bluetoothgamepad.domain.ProfileKind

enum class FacePosition { SOUTH,EAST,WEST,NORTH }

@Composable fun FaceButton(position:FacePosition,profile:ProfileKind,controlColor:Color=Color(0xFF6750A4),labelColor:Color=Color.White,onPressed:(Boolean)->Unit,modifier:Modifier=Modifier){
    var pressed by remember{mutableStateOf(false)}
    val xboxLabels=mapOf(FacePosition.SOUTH to "A",FacePosition.EAST to "B",FacePosition.WEST to "X",FacePosition.NORTH to "Y")
    val colors=mapOf(FacePosition.SOUTH to Color(0xFF55B849),FacePosition.EAST to Color(0xFFE44B4B),FacePosition.WEST to Color(0xFF438DD5),FacePosition.NORTH to Color(0xFFE8C94B))
    Canvas(modifier.size(58.dp).pointerInput(position,profile){detectTapGestures(onPress={pressed=true;onPressed(true);tryAwaitRelease();pressed=false;onPressed(false)})}){
        drawCircle(controlColor.copy(alpha=if(pressed).65f else .28f));drawCircle(colors[position]!!,style=Stroke(if(pressed)8f else 5f))
        if(profile==ProfileKind.PLAYSTATION){
            val c=center;val r=size.minDimension*.18f;val paint=labelColor
            when(position){
                FacePosition.SOUTH->{drawLine(paint,c-Offset(r,r),c+Offset(r,r),5f);drawLine(paint,c+Offset(r,-r),c+Offset(-r,r),5f)}
                FacePosition.EAST->drawCircle(paint,r,c,style=Stroke(5f))
                FacePosition.WEST->drawRect(paint,topLeft=c-Offset(r,r),size=androidx.compose.ui.geometry.Size(r*2,r*2),style=Stroke(5f))
                FacePosition.NORTH->{val p=Path().apply{moveTo(c.x,c.y-r);lineTo(c.x+r,c.y+r);lineTo(c.x-r,c.y+r);close()};drawPath(p,paint,style=Stroke(5f))}
            }
        }else {
            val labelPaint=android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply{color=labelColor.toArgb();textSize=size.minDimension*.42f;textAlign=android.graphics.Paint.Align.CENTER;isFakeBoldText=true}
            val baseline=center.y-(labelPaint.fontMetrics.ascent+labelPaint.fontMetrics.descent)/2f
            drawContext.canvas.nativeCanvas.drawText(xboxLabels[position]!!,center.x,baseline,labelPaint)
        }
    }
}
