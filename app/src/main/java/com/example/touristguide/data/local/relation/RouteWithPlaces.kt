package com.example.touristguide.data.local.relation

import com.example.touristguide.data.local.entity.PlaceEntity
import com.example.touristguide.data.local.entity.RouteEntity

data class RouteWithPlaces(
    val route: RouteEntity,
    val places: List<PlaceEntity>
)
