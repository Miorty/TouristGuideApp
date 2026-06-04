package com.example.touristguide.core.validation

import kotlin.math.abs

class DuplicateChecker {
    fun isPossibleDuplicate(title: String, latitude: Double, longitude: Double, existingTitle: String, existingLatitude: Double, existingLongitude: Double): Boolean {
        val sameTitle = title.equals(existingTitle, ignoreCase = true)
        val nearPoint = abs(latitude - existingLatitude) < 0.0005 && abs(longitude - existingLongitude) < 0.0005
        return sameTitle || nearPoint
    }
}
