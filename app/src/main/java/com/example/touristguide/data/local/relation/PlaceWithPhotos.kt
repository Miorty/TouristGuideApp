package com.example.touristguide.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.touristguide.data.local.entity.PlaceEntity
import com.example.touristguide.data.local.entity.PlacePhotoEntity

data class PlaceWithPhotos(
    @Embedded val place: PlaceEntity,
    @Relation(parentColumn = "id", entityColumn = "place_id") val photos: List<PlacePhotoEntity>
)
