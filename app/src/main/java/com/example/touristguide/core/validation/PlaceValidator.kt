package com.example.touristguide.core.validation

import com.example.touristguide.core.constants.ValidationConstants

class PlaceValidator(private val forbiddenWordsFilter: ForbiddenWordsFilter = ForbiddenWordsFilter()) {
    fun validate(title: String, description: String, latitude: Double?, longitude: Double?): ValidationResult {
        if (title.isBlank()) return ValidationResult.error("Введите название места")
        if (description.length < ValidationConstants.MIN_PLACE_DESCRIPTION_LENGTH) return ValidationResult.error("Описание места слишком короткое")
        if (latitude == null || longitude == null) return ValidationResult.error("Укажите координаты")
        if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) return ValidationResult.error("Координаты вне допустимого диапазона")
        if (forbiddenWordsFilter.hasForbiddenWords(title) || forbiddenWordsFilter.hasForbiddenWords(description)) return ValidationResult.error("Текст требует проверки")
        return ValidationResult.success()
    }
}
