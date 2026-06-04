package com.example.touristguide.data.repository

import com.example.touristguide.data.local.dao.PlaceDao
import com.example.touristguide.data.local.dao.RatingDao
import com.example.touristguide.data.local.entity.RatingEntity

class RatingRepository(private val ratingDao: RatingDao, private val placeDao: PlaceDao) {
    suspend fun ratePlace(rating: RatingEntity): RepositoryResult<Double> {
        if (rating.value !in 1..5) return RepositoryResult.failure("Оценка должна быть от 1 до 5")
        val existing = ratingDao.findUserRating(rating.userId, rating.placeId)
        if (existing == null) {
            ratingDao.insert(rating)
        } else {
            ratingDao.updateValue(existing.id, rating.value)
        }
        val average = ratingDao.getAverageRating(rating.placeId) ?: 0.0
        placeDao.updateAverageRating(rating.placeId, average)
        return RepositoryResult.success(average)
    }
}
