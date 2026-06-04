package com.example.touristguide.core.validation

import com.example.touristguide.core.constants.ValidationConstants

class ForbiddenWordsFilter {
    fun hasForbiddenWords(text: String): Boolean = ValidationConstants.FORBIDDEN_WORDS.any { word ->
        text.contains(word, ignoreCase = true)
    }
}
