package com.example.bluetoothgamepad.ui.controls

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.bluetoothgamepad.ui.skins.LocalPadSkin
import com.example.bluetoothgamepad.ui.skins.darken
import com.example.bluetoothgamepad.ui.skins.lighten

@Composable fun SmallOvalButton(label:String,controlColor:Color=Color(0xFF6750A4),labelColor:Color=Color.White,onPressed:(Boolean)->Unit){
    val skin=LocalPadSkin.current
    var down by remember{mutableStateOf(false)}
    Box(
        Modifier.size(66.dp,30.dp)
            .pointerInput(Unit){detectTapGestures(onPress={down=true;onPressed(true);tryAwaitRelease();down=false;onPressed(false)})},
        contentAlignment=Alignment.Center
    ){
        Canvas(Modifier.matchParentSize()){
            val radius=size.height/2f
            val outline=Path().apply{addRoundRect(androidx.compose.ui.geometry.RoundRect(Rect(Offset.Zero,size),CornerRadius(radius,radius)))}
            val base=if(down) lighten(controlColor,.18f) else controlColor
            drawPath(outline,darken(base,.35f))
            clipPath(outline){
                drawRect(
                    brush=Brush.verticalGradient(listOf(lighten(base,.30f),base,darken(base,.22f))),
                    topLeft=Offset.Zero,
                    size=size
                )
                if(skin.gloss) drawRect(
                    brush=Brush.verticalGradient(listOf(Color.White.copy(alpha=.28f),Color.Transparent),startY=0f,endY=size.height*.55f)
                )
            }
            drawPath(outline,labelColor.copy(alpha=.30f),style=Stroke(width=size.height*.06f))
        }
        Text(label,color=labelColor,style=MaterialTheme.typography.labelMedium)
    }
}
