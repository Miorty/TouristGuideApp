package com.example.touristguide.core.storage

import android.content.Context
import android.net.Uri
import java.io.File

class ImageStorageManager(private val context: Context) {
    fun saveImage(uri: Uri): String {
        val directory = File(context.filesDir, "place_photos").apply {
            if (!exists()) mkdirs()
        }
        val extension = context.contentResolver.getType(uri)?.substringAfterLast('/') ?: "jpg"
        val target = File(directory, "photo_${System.currentTimeMillis()}.$extension")

        context.contentResolver.openInputStream(uri)?.use { input ->
            target.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: return uri.toString()

        return target.absolutePath
    }
}
