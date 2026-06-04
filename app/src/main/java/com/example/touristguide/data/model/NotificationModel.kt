package com.example.touristguide.data.model

data class NotificationModel(val id: Long, val title: String, val text: String, val createdAt: Long = System.currentTimeMillis())
