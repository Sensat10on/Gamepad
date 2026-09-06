package com.example.bluetoothgamepad.ui.controls

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable fun SmallOvalButton(label:String,controlColor:Color=Color(0xFF6750A4),onPressed:(Boolean)->Unit){var down by remember{mutableStateOf(false)};Box(Modifier.size(66.dp,30.dp).background(if(down)controlColor else controlColor.copy(alpha=.55f),RoundedCornerShape(50)).pointerInput(Unit){detectTapGestures(onPress={down=true;onPressed(true);tryAwaitRelease();down=false;onPressed(false)})},contentAlignment=Alignment.Center){Text(label,color=Color.White)}}
