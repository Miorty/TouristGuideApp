package com.example.touristguide.core.util

object MapUtils {
    fun isValidPoint(latitude: Double, longitude: Double): Boolean = latitude in -90.0..90.0 && longitude in -180.0..180.0
}
