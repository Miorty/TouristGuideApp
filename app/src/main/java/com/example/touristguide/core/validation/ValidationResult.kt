package com.example.touristguide.core.validation

data class ValidationResult(val isValid: Boolean, val message: String? = null) {
    companion object {
        fun success() = ValidationResult(true)
        fun error(message: String) = ValidationResult(false, message)
    }
}
