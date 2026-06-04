package com.example.touristguide.core.storage

object FileUtils {
    fun fileNameFromPath(path: String): String = path.substringAfterLast('/')
}
