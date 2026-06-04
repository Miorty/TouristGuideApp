package com.example.touristguide

import com.example.touristguide.core.validation.ReviewValidator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentValidatorTest {
    @Test fun shortReviewIsInvalid() { assertFalse(ReviewValidator().validate("коротко").isValid) }
    @Test fun normalReviewIsValid() { assertTrue(ReviewValidator().validate("Хорошее место для прогулки и знакомства с городом.").isValid) }
}
