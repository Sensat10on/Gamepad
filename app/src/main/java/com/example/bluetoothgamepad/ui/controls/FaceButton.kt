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
import com.example.bluetoothgamepad.ui.skins.LocalPadSkin
import com.example.bluetoothgamepad.ui.skins.darken
import com.example.bluetoothgamepad.ui.skins.drawBall

enum class FacePosition { SOUTH,EAST,WEST,NORTH }

/**
 * @param playStationGlyphs draws ○ □ ✕ △ instead of A B X Y. Purely cosmetic — the HID report is
 *   identical either way.
 */
@Composable fun FaceButton(position:FacePosition,playStationGlyphs:Boolean,onPressed:(Boolean)->Unit,modifier:Modifier=Modifier,controlColor:Color=Color(0xFF6750A4),labelColor:Color=Color.White){
    val skin=LocalPadSkin.current
    var pressed by remember{mutableStateOf(false)}
    val letters=mapOf(FacePosition.SOUTH to "A",FacePosition.EAST to "B",FacePosition.WEST to "X",FacePosition.NORTH to "Y")
    val colors=mapOf(FacePosition.SOUTH to Color(0xFF55B849),FacePosition.EAST to Color(0xFFE44B4B),FacePosition.WEST to Color(0xFF438DD5),FacePosition.NORTH to Color(0xFFE8C94B))
    Canvas(modifier.size(58.dp).pointerInput(position,playStationGlyphs){detectTapGestures(onPress={pressed=true;onPressed(true);tryAwaitRelease();pressed=false;onPressed(false)})}){
        val r=size.minDimension/2f
        val c=center
        val base=colors[position]!!
        drawBall(if(pressed) darken(base,.18f) else base,r*.92f,c,skin.gloss)
        if(playStationGlyphs){
            val gr=r*.30f;val paint=labelColor
            when(position){
                FacePosition.SOUTH->{drawLine(paint,c-Offset(gr,gr),c+Offset(gr,gr),r*.13f);drawLine(paint,c+Offset(gr,-gr),c+Offset(-gr,gr),r*.13f)}
                FacePosition.EAST->drawCircle(paint,gr,c,style=Stroke(r*.13f))
                FacePosition.WEST->drawRect(paint,topLeft=c-Offset(gr,gr),size=androidx.compose.ui.geometry.Size(gr*2,gr*2),style=Stroke(r*.13f))
                FacePosition.NORTH->{val p=Path().apply{moveTo(c.x,c.y-gr);lineTo(c.x+gr,c.y+gr);lineTo(c.x-gr,c.y+gr);close()};drawPath(p,paint,style=Stroke(r*.13f))}
            }
        }else {
            val labelPaint=android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply{color=labelColor.toArgb();textSize=r*.88f;textAlign=android.graphics.Paint.Align.CENTER;isFakeBoldText=true}
            val baseline=c.y-(labelPaint.fontMetrics.ascent+labelPaint.fontMetrics.descent)/2f
            drawContext.canvas.nativeCanvas.drawText(letters[position]!!,c.x,baseline,labelPaint)
        }
    }
}
