package com.example.bluetoothgamepad.ui

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import com.example.bluetoothgamepad.data.GamepadSettings

@Composable fun WallpaperLayer(settings:GamepadSettings,dimming:Float=.42f){
    Box(Modifier.fillMaxSize().background(Color(settings.backgroundColor.toInt())))
    WallpaperLayer(settings.backgroundUri,settings.wallpaperZoom,settings.wallpaperOffsetX,settings.wallpaperOffsetY,dimming)
}

@Composable fun WallpaperLayer(uri:String?,zoom:Float,offsetX:Float,offsetY:Float,dimming:Float=.42f){
    val context=LocalContext.current
    val bitmap=remember(uri){uri?.let{value->runCatching{context.contentResolver.openInputStream(Uri.parse(value))?.use{BitmapFactory.decodeStream(it)?.asImageBitmap()}}.getOrNull()}}
    if(bitmap!=null) BoxWithConstraints(Modifier.fillMaxSize().clipToBounds()){
        val density=LocalDensity.current;val widthPx=with(density){maxWidth.toPx()};val heightPx=with(density){maxHeight.toPx()}
        Image(bitmap,null,Modifier.matchParentSize().graphicsLayer(scaleX=zoom.coerceIn(1f,3f),scaleY=zoom.coerceIn(1f,3f),translationX=offsetX.coerceIn(-1f,1f)*widthPx*.35f,translationY=offsetY.coerceIn(-1f,1f)*heightPx*.35f),contentScale=ContentScale.Crop)
        if(dimming>0f)Box(Modifier.matchParentSize().background(Color.Black.copy(alpha=dimming)))
    }
}
