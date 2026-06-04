package com.example.touristguide.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "moderation_queue",
    indices = [
        Index("moderator_id"),
        Index("entity_id"),
        Index("status"),
        Index(value = ["entity_type", "entity_id"])
    ]
)
data class ModerationQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "moderator_id") val moderatorId: Long? = null,
    @ColumnInfo(name = "entity_type") val entityType: String,
    @ColumnInfo(name = "entity_id") val entityId: Long,
    val status: String = "PENDING",
    val comment: String = "",
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "checked_at") val checkedAt: Long? = null
)
