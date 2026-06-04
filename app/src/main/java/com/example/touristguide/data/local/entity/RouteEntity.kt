package com.example.touristguide.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "routes",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["author_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("author_id"), Index("status")]
)
data class RouteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "author_id") val authorId: Long,
    val title: String,
    val description: String,
    @ColumnInfo(name = "duration_minutes") val durationMinutes: Int,
    @ColumnInfo(name = "distance_km") val distanceKm: Double,
    val status: String = "PUBLISHED",
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)
