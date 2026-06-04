package com.example.touristguide.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.touristguide.data.local.entity.CategoryEntity
import com.example.touristguide.data.local.entity.PlaceEntity

data class PlaceWithCategory(
    @Embedded val place: PlaceEntity,
    @Relation(parentColumn = "category_id", entityColumn = "id") val category: CategoryEntity?
)
