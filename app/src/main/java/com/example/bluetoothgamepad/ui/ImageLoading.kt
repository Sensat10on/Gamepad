package com.example.bluetoothgamepad.ui

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Decodes [uri] at (roughly) the requested resolution on a background dispatcher.
 *
 * Two passes: the first only reads the bounds so [BitmapFactory.Options.inSampleSize] can be
 * chosen, the second decodes. Returns null when the persisted content URI is no longer readable,
 * which also covers a wallpaper reference restored from a backup.
 */
@Composable
fun rememberDownsampledImage(uri:String?,targetWidth:Int,targetHeight:Int):ImageBitmap?{
    val context=LocalContext.current
    val bitmap by produceState<ImageBitmap?>(initialValue=null,uri,targetWidth,targetHeight){
        value=withContext(Dispatchers.IO){decodeSampled(context,uri,targetWidth,targetHeight)}
    }
    return bitmap
}

/** Decoding a full-resolution photo inline is an OOM/jank risk, so always sample it down first. */
fun decodeSampled(context:Context,uri:String?,targetWidth:Int,targetHeight:Int):ImageBitmap?{
    if(uri==null||targetWidth<=0||targetHeight<=0)return null
    val parsed=runCatching{Uri.parse(uri)}.getOrNull()?:return null
    return runCatching{
        val bounds=BitmapFactory.Options().apply{inJustDecodeBounds=true}
        context.contentResolver.openInputStream(parsed)?.use{BitmapFactory.decodeStream(it,null,bounds)}
        if(bounds.outWidth<=0||bounds.outHeight<=0)return@runCatching null
        var sample=1
        while(bounds.outWidth/(sample*2)>=targetWidth && bounds.outHeight/(sample*2)>=targetHeight) sample*=2
        val options=BitmapFactory.Options().apply{inSampleSize=sample}
        context.contentResolver.openInputStream(parsed)?.use{BitmapFactory.decodeStream(it,null,options)}?.asImageBitmap()
    }.getOrNull()
}
