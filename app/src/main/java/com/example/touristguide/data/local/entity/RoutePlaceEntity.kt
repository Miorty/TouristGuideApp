package com.example.touristguide.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "route_places",
    foreignKeys = [
        ForeignKey(
            entity = RouteEntity::class,
            parentColumns = ["id"],
            childColumns = ["route_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PlaceEntity::class,
            parentColumns = ["id"],
            childColumns = ["place_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("route_id"), Index("place_id"), Index(value = ["route_id", "position"], unique = true)]
)
data class RoutePlaceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "route_id") val routeId: Long,
    @ColumnInfo(name = "place_id") val placeId: Long,
    val position: Int
)
