package com.example.bluetoothgamepad.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import com.example.bluetoothgamepad.data.GamepadSettings

@Composable fun WallpaperLayer(settings:GamepadSettings,dimming:Float=.42f){
    Box(Modifier.fillMaxSize().background(Color(settings.backgroundColor.toInt())))
    WallpaperLayer(settings.backgroundUri,settings.wallpaperZoom,settings.wallpaperOffsetX,settings.wallpaperOffsetY,dimming)
}

@Composable fun WallpaperLayer(uri:String?,zoom:Float,offsetX:Float,offsetY:Float,dimming:Float=.42f){
    val density=LocalDensity.current
    // containerSize is the real window size; Configuration.screenWidthDp is rounded and inset
    // dependent, which is exactly what we do not want when sizing a decode target.
    val windowSize=LocalWindowInfo.current.containerSize
    val bitmap=rememberDownsampledImage(uri,windowSize.width,windowSize.height)

    val current=bitmap
    if(current!=null) BoxWithConstraints(Modifier.fillMaxSize().clipToBounds()){
        val widthPx=with(density){maxWidth.toPx()};val heightPx=with(density){maxHeight.toPx()}
        Image(current,null,Modifier.matchParentSize().graphicsLayer(scaleX=zoom.coerceIn(1f,3f),scaleY=zoom.coerceIn(1f,3f),translationX=offsetX.coerceIn(-1f,1f)*widthPx*.35f,translationY=offsetY.coerceIn(-1f,1f)*heightPx*.35f),contentScale=ContentScale.Crop)
        if(dimming>0f)Box(Modifier.matchParentSize().background(Color.Black.copy(alpha=dimming)))
    }
}
