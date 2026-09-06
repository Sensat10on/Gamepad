package com.example.bluetoothgamepad.ui.controls

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.unit.dp
import kotlin.math.round

enum class TriggerDirection { INCREASE_LEFT, INCREASE_RIGHT }

@Composable fun TriggerControl(label:String,direction:TriggerDirection?=null,step:Float=.05f,controlColor:Color=Color(0xFF6750A4),onValue:(Float)->Unit) {
    var value by remember { mutableFloatStateOf(0f) }
    val width=if(direction==null)82.dp else 164.dp
    fun quantize(raw:Float):Float { val safe=step.coerceIn(.01f,.25f);return (round(raw/safe)*safe).coerceIn(0f,1f) }
    Box(Modifier.size(width,44.dp).background(controlColor.copy(alpha=.48f),RoundedCornerShape(50)).pointerInput(direction,step){
        awaitEachGesture {
            val down=awaitFirstDown()
            value=if(direction==null)1f else quantize(if(direction==TriggerDirection.INCREASE_LEFT)1f-down.position.x/size.width else down.position.x/size.width)
            onValue(value)
            do { val event=awaitPointerEvent();val change=event.changes.firstOrNull{it.id==down.id}?:break;if(!change.pressed)break
                value=if(direction==null)1f else quantize(if(direction==TriggerDirection.INCREASE_LEFT)1f-change.position.x/size.width else change.position.x/size.width)
                onValue(value);change.consume()
            }while(true)
            value=0f;onValue(0f)
        }
    },contentAlignment=Alignment.Center){
        if(direction!=null) Box(Modifier.fillMaxHeight().fillMaxWidth(value).align(if(direction==TriggerDirection.INCREASE_LEFT)Alignment.CenterStart else Alignment.CenterEnd).background(controlColor,RoundedCornerShape(50)))
        else if(value>0f) Box(Modifier.fillMaxSize().background(controlColor,RoundedCornerShape(50)))
        Text(if(direction==null)label else "$label ${(value*100).toInt()}%",color=Color.White)
    }
}
