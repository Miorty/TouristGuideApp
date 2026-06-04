package com.example.touristguide.core.moderation

class ModerationManager {
    fun approve(comment: String = "") = ModerationResult(true, comment)
    fun reject(comment: String) = ModerationResult(false, comment)
}
