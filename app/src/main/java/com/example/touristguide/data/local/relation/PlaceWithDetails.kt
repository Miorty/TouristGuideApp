package com.example.touristguide.data.local.relation

import com.example.touristguide.data.local.entity.PlaceEntity
import com.example.touristguide.data.local.entity.PlacePhotoEntity
import com.example.touristguide.data.local.entity.ReviewEntity

data class PlaceWithDetails(
    val place: PlaceEntity,
    val photos: List<PlacePhotoEntity> = emptyList(),
    val reviews: List<ReviewEntity> = emptyList(),
    val isFavorite: Boolean = false
)
