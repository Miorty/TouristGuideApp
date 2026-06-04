package com.example.touristguide.core.util

object TextUtils {
    fun short(value: String, limit: Int = 80): String = if (value.length <= limit) value else value.take(limit) + "…"
}
