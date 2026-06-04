package com.example.touristguide.data.model

data class FeedItemModel(val id: Long, val author: String, val text: String, val placeTitle: String, val imagePath: String? = null, val likes: Int = 0, val views: Int = 0)
