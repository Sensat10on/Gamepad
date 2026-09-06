package com.example.bluetoothgamepad.ui.controls

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable fun SegaButton(label:String,controlColor:Color,labelColor:Color,onPressed:(Boolean)->Unit){
    var pressed by remember{mutableStateOf(false)}
    Canvas(Modifier.size(62.dp).pointerInput(label){detectTapGestures(onPress={pressed=true;onPressed(true);tryAwaitRelease();pressed=false;onPressed(false)})}){
        drawCircle(controlColor.copy(alpha=if(pressed).85f else .48f));drawCircle(labelColor.copy(alpha=.85f),style=Stroke(4f))
        val paint=android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply{color=labelColor.toArgb();textSize=size.minDimension*.43f;textAlign=android.graphics.Paint.Align.CENTER;isFakeBoldText=true}
        val baseline=center.y-(paint.fontMetrics.ascent+paint.fontMetrics.descent)/2f
        drawContext.canvas.nativeCanvas.drawText(label,center.x,baseline,paint)
    }
}
