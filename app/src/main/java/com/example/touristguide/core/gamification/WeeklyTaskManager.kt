package com.example.touristguide.core.gamification

class WeeklyTaskManager {
    fun isCompleted(progress: Int, target: Int): Boolean = progress >= target
}
