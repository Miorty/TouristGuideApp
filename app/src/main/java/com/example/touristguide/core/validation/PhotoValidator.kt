package com.example.touristguide.core.validation

class PhotoValidator {
    fun validate(path: String?): ValidationResult = if (path.isNullOrBlank()) ValidationResult.error("Выберите фотографию") else ValidationResult.success()
}
