package com.example.touristguide.core.validation

class ContentValidator {
    fun notBlank(value: String, message: String): ValidationResult = if (value.isBlank()) ValidationResult.error(message) else ValidationResult.success()
}
