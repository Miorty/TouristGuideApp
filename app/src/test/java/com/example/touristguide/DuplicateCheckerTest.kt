package com.example.touristguide

import com.example.touristguide.core.validation.DuplicateChecker
import org.junit.Assert.assertTrue
import org.junit.Test

class DuplicateCheckerTest {
    @Test fun nearPointIsDuplicate() { assertTrue(DuplicateChecker().isPossibleDuplicate("Дом", 58.0, 56.0, "Дом", 58.0001, 56.0001)) }
}
