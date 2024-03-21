package com.alexsullivan.datacollor.utils

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayInputStream

fun byteArrayToImageBitmap(byteArray: ByteArray): ImageBitmap {
    val inputStream = ByteArrayInputStream(byteArray)
    val bitmap = BitmapFactory.decodeStream(inputStream)
    return bitmap.asImageBitmap()
}
