package com.example.touristguide.data.local.relation

import com.example.touristguide.data.local.entity.AchievementEntity
import com.example.touristguide.data.local.entity.UserEntity

data class UserWithAchievements(
    val user: UserEntity,
    val achievements: List<AchievementEntity>
)
