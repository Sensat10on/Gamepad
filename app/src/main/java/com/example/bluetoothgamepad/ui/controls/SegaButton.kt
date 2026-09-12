package com.example.bluetoothgamepad.ui.controls

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.bluetoothgamepad.ui.skins.LocalPadSkin
import com.example.bluetoothgamepad.ui.skins.darken
import com.example.bluetoothgamepad.ui.skins.drawBall

@Composable fun SegaButton(label:String,controlColor:Color,labelColor:Color,onPressed:(Boolean)->Unit){
    val skin=LocalPadSkin.current
    var pressed by remember{mutableStateOf(false)}
    Canvas(Modifier.size(62.dp).pointerInput(label){detectTapGestures(onPress={pressed=true;onPressed(true);tryAwaitRelease();pressed=false;onPressed(false)})}){
        val r=size.minDimension/2f
        val c=center
        drawBall(if(pressed) darken(controlColor,.16f) else controlColor,r*.92f,c,skin.gloss)
        val paint=android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply{color=labelColor.toArgb();textSize=r*.90f;textAlign=android.graphics.Paint.Align.CENTER;isFakeBoldText=true}
        val baseline=c.y-(paint.fontMetrics.ascent+paint.fontMetrics.descent)/2f
        drawContext.canvas.nativeCanvas.drawText(label,c.x,baseline,paint)
    }
}
