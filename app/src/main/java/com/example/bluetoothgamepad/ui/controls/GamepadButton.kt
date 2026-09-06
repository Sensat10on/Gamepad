package com.example.bluetoothgamepad.ui.controls

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable fun GamepadButton(label:String, color:Color=Color(0xFF6750A4), onPressed:(Boolean)->Unit, modifier:Modifier=Modifier) {
    var pressed by remember { mutableStateOf(false) }
    Box(modifier.size(58.dp).background(if(pressed) color.copy(alpha=.6f) else color,CircleShape)
        .pointerInput(Unit){ detectTapGestures(onPress={ pressed=true; onPressed(true); tryAwaitRelease(); pressed=false; onPressed(false) }) }, contentAlignment=Alignment.Center) {
        Text(label,color=Color.White)
    }
}
