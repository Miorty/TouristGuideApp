package com.example.touristguide.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    @ColumnInfo(name = "task_type") val taskType: String,
    @ColumnInfo(name = "target_value") val targetValue: Int,
    @ColumnInfo(name = "points_reward") val pointsReward: Int,
    @ColumnInfo(name = "is_weekly") val isWeekly: Boolean = false
)
