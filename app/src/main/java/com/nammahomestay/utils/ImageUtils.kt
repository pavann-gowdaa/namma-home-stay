package com.nammahomestay.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

object ImageUtils {

    suspend fun compressImage(context: Context, imageUri: Uri, maxSizeKB: Int = 500): ByteArray {
        return withContext(Dispatchers.IO) {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            var quality = 100
            var stream = ByteArrayOutputStream()

            bitmap?.let {
                it.compress(Bitmap.CompressFormat.JPEG, quality, stream)
                while (stream.toByteArray().size > maxSizeKB * 1024 && quality > 10) {
                    stream.reset()
                    quality -= 10
                    it.compress(Bitmap.CompressFormat.JPEG, quality, stream)
                }
            }

            stream.toByteArray()
        }
    }

    fun getBitmapSizeInKB(bitmap: Bitmap): Int {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray().size / 1024
    }
}
