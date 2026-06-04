package com.example.touristguide.data.model

data class PlaceCardModel(val id: Long, val title: String, val description: String, val rating: Double, val imagePath: String? = null, val isFavorite: Boolean = false)
