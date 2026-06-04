package com.example.touristguide

import com.example.touristguide.core.gamification.AchievementChecker
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementCheckerTest {
    @Test fun unlockWhenConditionReached() { assertTrue(AchievementChecker().shouldUnlock("REVIEWS_COUNT", 1, 1)) }
}
