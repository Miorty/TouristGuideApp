package com.example.touristguide.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    @ColumnInfo(name = "condition_type") val conditionType: String,
    @ColumnInfo(name = "condition_value") val conditionValue: Int,
    @ColumnInfo(name = "points_reward") val pointsReward: Int
)
