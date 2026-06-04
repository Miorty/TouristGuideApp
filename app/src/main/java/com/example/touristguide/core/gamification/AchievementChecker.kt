package com.example.touristguide.core.gamification

class AchievementChecker {
    fun shouldUnlock(conditionType: String, conditionValue: Int, currentValue: Int): Boolean = currentValue >= conditionValue
}
