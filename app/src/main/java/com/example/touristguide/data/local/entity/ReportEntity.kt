package com.example.touristguide.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reports",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PlaceEntity::class,
            parentColumns = ["id"],
            childColumns = ["place_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = ReviewEntity::class,
            parentColumns = ["id"],
            childColumns = ["review_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = PlacePhotoEntity::class,
            parentColumns = ["id"],
            childColumns = ["photo_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("user_id"), Index("place_id"), Index("review_id"), Index("photo_id"), Index("status")]
)
data class ReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "user_id") val userId: Long,
    @ColumnInfo(name = "place_id") val placeId: Long? = null,
    @ColumnInfo(name = "review_id") val reviewId: Long? = null,
    @ColumnInfo(name = "photo_id") val photoId: Long? = null,
    @ColumnInfo(name = "report_type") val reportType: String,
    val comment: String = "",
    val status: String = "NEW",
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)
