package com.example.bluetoothgamepad.ui.controls

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.unit.dp
import com.example.bluetoothgamepad.input.StickMath
import com.example.bluetoothgamepad.ui.skins.LocalPadSkin
import com.example.bluetoothgamepad.ui.skins.drawBall
import com.example.bluetoothgamepad.ui.skins.lighten
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable fun AnalogStick(deadZone: Float, sensitivity: Float, invertY: Boolean, holdDelayMs:Int, controlColor:Color, onStickPressed:(Boolean)->Unit, onValue: (Float,Float)->Unit, modifier: Modifier = Modifier) {
    val skin=LocalPadSkin.current
    var knob by remember { mutableStateOf(Offset.Zero) }
    val holdScope=rememberCoroutineScope()
    Canvas(modifier.size(150.dp).pointerInput(deadZone,sensitivity,invertY,holdDelayMs) {
        awaitEachGesture {
            val down = awaitFirstDown(); val center = Offset(size.width/2f,size.height/2f); val radius=minOf(size.width,size.height)/2f
            var holdActivated=false
            val holdJob=holdScope.launch { delay(holdDelayMs.coerceIn(0,3000).toLong());holdActivated=true;onStickPressed(true) }
            // finally: a cancelled gesture (window focus loss, activity stop, pointer reset) must
            // still release the stick and L3/R3 on the host.
            try {
                while(true) {
                    val event=awaitPointerEvent(); val change=event.changes.firstOrNull { it.id == down.id } ?: break
                    if (!change.pressed) break
                    val v=StickMath.normalize(change.position.x-center.x,change.position.y-center.y,radius,deadZone,sensitivity)
                    knob=Offset(v.x*radius,v.y*radius); onValue(v.x,if(invertY) -v.y else v.y); change.consume()
                }
            } finally {
                holdJob.cancel();if(holdActivated)onStickPressed(false);knob=Offset.Zero; onValue(0f,0f)
            }
        }
    }) {
        // Just the cap: the socket circle and its inner rim ring used to read as extra buttons
        // underneath the stick, and they collided with the face buttons next to them.
        val r=size.minDimension/2f
        val capRadius=r*.75f
        val capCenter=center+knob*.25f
        drawBall(lighten(controlColor,.10f),capRadius,capCenter,skin.gloss)
    }
}
