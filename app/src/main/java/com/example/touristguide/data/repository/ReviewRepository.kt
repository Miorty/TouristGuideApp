package com.example.touristguide.data.repository

import androidx.room.withTransaction
import com.example.touristguide.core.enums.ActivityActionType
import com.example.touristguide.core.enums.ContentStatus
import com.example.touristguide.core.enums.ModerationEntityType
import com.example.touristguide.core.validation.ReviewValidator
import com.example.touristguide.data.local.AppDatabase
import com.example.touristguide.data.local.dao.ActivityLogDao
import com.example.touristguide.data.local.dao.ModerationDao
import com.example.touristguide.data.local.dao.ReviewDao
import com.example.touristguide.data.local.entity.ActivityLogEntity
import com.example.touristguide.data.local.entity.ModerationQueueEntity
import com.example.touristguide.data.local.entity.ReviewEntity
import kotlinx.coroutines.flow.Flow

class ReviewRepository(
    private val database: AppDatabase,
    private val reviewDao: ReviewDao,
    private val moderationDao: ModerationDao,
    private val activityLogDao: ActivityLogDao,
    private val validator: ReviewValidator = ReviewValidator()
) {
    fun observePlaceReviews(placeId: Long): Flow<List<ReviewEntity>> = reviewDao.observeByPlace(placeId)

    suspend fun addReview(placeId: Long, userId: Long, text: String): RepositoryResult<ReviewEntity> {
        val result = validator.validate(text)
        if (!result.isValid) return RepositoryResult.failure(result.message ?: "Некорректный отзыв")

        val saved = database.withTransaction {
            val review = ReviewEntity(
                placeId = placeId,
                userId = userId,
                text = text.trim(),
                status = ContentStatus.PENDING.name
            )
            val reviewId = reviewDao.insert(review)
            moderationDao.insert(ModerationQueueEntity(entityType = ModerationEntityType.REVIEW.name, entityId = reviewId))
            activityLogDao.insert(
                ActivityLogEntity(
                    userId = userId,
                    actionType = ActivityActionType.ADD_REVIEW.name,
                    entityType = ModerationEntityType.REVIEW.name,
                    entityId = reviewId
                )
            )
            review.copy(id = reviewId)
        }
        return RepositoryResult.success(saved)
    }
}
