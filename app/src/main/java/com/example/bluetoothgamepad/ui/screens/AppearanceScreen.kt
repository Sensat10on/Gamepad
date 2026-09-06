package com.example.bluetoothgamepad.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.bluetoothgamepad.data.GamepadSettings
import com.example.bluetoothgamepad.ui.UiStrings
import com.example.bluetoothgamepad.ui.WallpaperLayer

@Composable fun AppearanceScreen(settings:GamepadSettings,text:UiStrings,onSave:(String?,Long,Float,Float,Float,Boolean,Long,Long,Boolean,Long,Long)->Unit){
    val context=LocalContext.current
    var backgroundUri by remember(settings.backgroundUri){mutableStateOf(settings.backgroundUri)}
    var controlColor by remember(settings.controlColor){mutableLongStateOf(settings.controlColor)}
    var zoom by remember(settings.wallpaperZoom){mutableFloatStateOf(settings.wallpaperZoom)}
    var offsetX by remember(settings.wallpaperOffsetX){mutableFloatStateOf(settings.wallpaperOffsetX)}
    var offsetY by remember(settings.wallpaperOffsetY){mutableFloatStateOf(settings.wallpaperOffsetY)}
    var automaticTextColor by remember(settings.automaticTextColor){mutableStateOf(settings.automaticTextColor)}
    var textColor by remember(settings.textColor){mutableLongStateOf(settings.textColor)}
    var backgroundColor by remember(settings.backgroundColor){mutableLongStateOf(settings.backgroundColor)}
    var darkTheme by remember(settings.darkTheme){mutableStateOf(settings.darkTheme)}
    var buttonLabelColor by remember(settings.buttonLabelColor){mutableLongStateOf(settings.buttonLabelColor)}
    var dpadLineColor by remember(settings.dpadLineColor){mutableLongStateOf(settings.dpadLineColor)}
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->uri?.let{
        runCatching{context.contentResolver.takePersistableUriPermission(it,Intent.FLAG_GRANT_READ_URI_PERMISSION)}
        backgroundUri=it.toString()
    }}
    val colors=listOf(0xFF6750A4L,0xFF1565C0L,0xFF00897BL,0xFF2E7D32L,0xFFC62828L,0xFFF57C00L,0xFF616161L,0xFFE91E63L)
    val textColors=listOf(0xFFFFFFFFL,0xFF000000L,0xFFFFF176L,0xFF80D8FFL,0xFFA7FFEBL,0xFFFF8A80L)
    val backgroundColors=listOf(0xFF121016L,0xFF000000L,0xFF202124L,0xFF263238L,0xFF0D1B2AL,0xFF2B193DL,0xFFF5F5F5L,0xFFFFF8E1L)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),verticalArrangement=Arrangement.spacedBy(18.dp)){
        Text(text.chooseBackground,style=MaterialTheme.typography.titleLarge)
        Column(verticalArrangement=Arrangement.spacedBy(8.dp)){Button(onClick={picker.launch(arrayOf("image/*"))}){Text(text.chooseBackground)};if(backgroundUri!=null)OutlinedButton(onClick={backgroundUri=null}){Text(text.removeBackground)}}
        backgroundUri?.let{Text(it.substringAfterLast('/'),style=MaterialTheme.typography.bodySmall)}
        if(backgroundUri==null){
            val backgroundLabel=when(settings.language){"uk"->"Колір заднього фону";"en"->"Background color";else->"Цвет заднего фона"}
            Text(backgroundLabel,style=MaterialTheme.typography.titleLarge)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(14.dp)){backgroundColors.forEach{argb->Box(Modifier.size(54.dp).background(Color(argb.toInt()),CircleShape).border(if(backgroundColor==argb)4.dp else 1.dp,Color.Gray,CircleShape).clickable{backgroundColor=argb})}}
        }
        if(backgroundUri!=null){
            Box(Modifier.fillMaxWidth().height(180.dp).border(1.dp,MaterialTheme.colorScheme.outline).pointerInput(backgroundUri){detectTransformGestures{_,pan,gestureZoom,_->zoom=(zoom*gestureZoom).coerceIn(1f,3f);offsetX=(offsetX+pan.x/500f).coerceIn(-1f,1f);offsetY=(offsetY+pan.y/500f).coerceIn(-1f,1f)}}){WallpaperLayer(backgroundUri,zoom,offsetX,offsetY,0f)}
            val cropLabels=when(settings.language){"uk"->listOf("Масштаб","Горизонтальне положення","Вертикальне положення");"en"->listOf("Zoom","Horizontal position","Vertical position");else->listOf("Масштаб","Горизонтальное положение","Вертикальное положение")}
            Text(when(settings.language){"uk"->"Щипок — масштаб, перетягування — кадрування";"en"->"Pinch to zoom, drag to crop";else->"Щипок — масштаб, перетаскивание — кадрирование"},style=MaterialTheme.typography.bodySmall)
            Text("${cropLabels[0]}: ${"%.1f".format(zoom)}×");Slider(zoom,{zoom=it},valueRange=1f..3f)
            Text(cropLabels[1]);Slider(offsetX,{offsetX=it},valueRange=-1f..1f)
            Text(cropLabels[2]);Slider(offsetY,{offsetY=it},valueRange=-1f..1f)
        }
        Text(text.buttonColor,style=MaterialTheme.typography.titleLarge)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(14.dp)){colors.forEach{argb->Box(Modifier.size(54.dp).background(Color(argb.toInt()),CircleShape).border(if(controlColor==argb)4.dp else 1.dp,Color.White,CircleShape).clickable{controlColor=argb})}}
        val fontLabels=when(settings.language){"uk"->listOf("Автоматичний колір тексту","Колір тексту");"en"->listOf("Automatic text color","Text color");else->listOf("Автоматический цвет текста","Цвет текста")}
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Text(fontLabels[0]);Switch(automaticTextColor,{automaticTextColor=it})}
        if(!automaticTextColor){Text(fontLabels[1],style=MaterialTheme.typography.titleLarge);Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(14.dp)){textColors.forEach{argb->Box(Modifier.size(54.dp).background(Color(argb.toInt()),CircleShape).border(if(textColor==argb)4.dp else 1.dp,Color.Gray,CircleShape).clickable{textColor=argb})}}}
        val detailLabels=when(settings.language){"uk"->listOf("Колір символів на кнопках","Колір ліній D-pad");"en"->listOf("Button label color","D-pad line color");else->listOf("Цвет символов на кнопках","Цвет линий D-pad")}
        Text(detailLabels[0],style=MaterialTheme.typography.titleLarge);Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(14.dp)){textColors.forEach{argb->Box(Modifier.size(54.dp).background(Color(argb.toInt()),CircleShape).border(if(buttonLabelColor==argb)4.dp else 1.dp,Color.Gray,CircleShape).clickable{buttonLabelColor=argb})}}
        Text(detailLabels[1],style=MaterialTheme.typography.titleLarge);Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(14.dp)){textColors.forEach{argb->Box(Modifier.size(54.dp).background(Color(argb.toInt()),CircleShape).border(if(dpadLineColor==argb)4.dp else 1.dp,Color.Gray,CircleShape).clickable{dpadLineColor=argb})}}
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Text(text.darkTheme);Switch(darkTheme,{darkTheme=it})}
        Button(onClick={onSave(backgroundUri,controlColor,zoom,offsetX,offsetY,automaticTextColor,textColor,backgroundColor,darkTheme,buttonLabelColor,dpadLineColor)}){Text(text.save)}
    }
}
