package com.example.touristguide.core.validation

import com.example.touristguide.core.constants.ValidationConstants

class ReviewValidator {
    fun validate(text: String): ValidationResult = when {
        text.length < ValidationConstants.MIN_REVIEW_LENGTH -> ValidationResult.error("Отзыв должен содержать не менее 20 символов")
        else -> ValidationResult.success()
    }
}
