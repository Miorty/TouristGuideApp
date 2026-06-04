package com.example.touristguide.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val email: String,
    val password: String,
    val role: String = "USER",
    val points: Int = 0,
    val level: Int = 1,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)
