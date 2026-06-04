package com.example.touristguide.core.gamification

class PointsManager {
    fun calculateLevel(points: Int): Int = 1 + points / 100
    fun pointsForApprovedPlace(): Int = 30
    fun pointsForApprovedReview(): Int = 10
    fun pointsForApprovedPhoto(): Int = 10
}
