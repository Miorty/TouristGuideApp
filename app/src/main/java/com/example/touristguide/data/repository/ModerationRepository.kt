package com.example.touristguide.data.repository

import androidx.room.withTransaction
import com.example.touristguide.core.enums.ActivityActionType
import com.example.touristguide.core.enums.ContentStatus
import com.example.touristguide.core.enums.ModerationEntityType
import com.example.touristguide.core.enums.ReportStatus
import com.example.touristguide.core.gamification.AchievementChecker
import com.example.touristguide.core.gamification.PointsManager
import com.example.touristguide.data.local.AppDatabase
import com.example.touristguide.data.local.dao.AchievementDao
import com.example.touristguide.data.local.dao.ActivityLogDao
import com.example.touristguide.data.local.dao.ModerationDao
import com.example.touristguide.data.local.dao.PlacePhotoDao
import com.example.touristguide.data.local.dao.PlaceDao
import com.example.touristguide.data.local.dao.ReportDao
import com.example.touristguide.data.local.dao.ReviewDao
import com.example.touristguide.data.local.dao.RouteDao
import com.example.touristguide.data.local.dao.UserDao
import com.example.touristguide.data.local.entity.ActivityLogEntity
import com.example.touristguide.data.local.entity.AchievementEntity
import com.example.touristguide.data.local.entity.ModerationQueueEntity
import com.example.touristguide.data.local.entity.UserAchievementEntity
import kotlinx.coroutines.flow.Flow

class ModerationRepository(
    private val database: AppDatabase,
    private val moderationDao: ModerationDao,
    private val placeDao: PlaceDao,
    private val placePhotoDao: PlacePhotoDao,
    private val reviewDao: ReviewDao,
    private val reportDao: ReportDao,
    private val routeDao: RouteDao,
    private val userDao: UserDao,
    private val achievementDao: AchievementDao,
    private val activityLogDao: ActivityLogDao,
    private val pointsManager: PointsManager = PointsManager(),
    private val achievementChecker: AchievementChecker = AchievementChecker()
) {
    fun observePending(): Flow<List<ModerationQueueEntity>> = moderationDao.observePending()

    suspend fun approve(queueId: Long, moderatorId: Long, comment: String = ""): RepositoryResult<ModerationQueueEntity> {
        val queueItem = moderationDao.getById(queueId) ?: return RepositoryResult.failure("Запись модерации не найдена")
        database.withTransaction {
            updateEntityStatus(queueItem, approved = true)
            moderationDao.updateDecision(queueId, "APPROVED", moderatorId, comment)
            val authorId = resolveAuthorId(queueItem)
            if (authorId != null) {
                val points = pointsFor(queueItem.entityType)
                addPoints(authorId, points)
                val condition = conditionFor(queueItem.entityType)
                val currentValue = currentValueFor(authorId, queueItem.entityType)
                if (condition != null) unlockAchievements(authorId, condition, currentValue)
                activityLogDao.insert(
                    ActivityLogEntity(
                        userId = authorId,
                        actionType = ActivityActionType.POINTS_ADDED.name,
                        entityType = queueItem.entityType,
                        entityId = queueItem.entityId,
                        pointsChange = points
                    )
                )
            }
            activityLogDao.insert(
                ActivityLogEntity(
                    userId = moderatorId,
                    actionType = ActivityActionType.APPROVE_CONTENT.name,
                    entityType = queueItem.entityType,
                    entityId = queueItem.entityId
                )
            )
        }
        return RepositoryResult.success(queueItem.copy(status = "APPROVED", moderatorId = moderatorId, comment = comment, checkedAt = System.currentTimeMillis()))
    }

    suspend fun reject(queueId: Long, moderatorId: Long, comment: String): RepositoryResult<ModerationQueueEntity> {
        if (comment.isBlank()) return RepositoryResult.failure("Укажите причину отклонения")
        val queueItem = moderationDao.getById(queueId) ?: return RepositoryResult.failure("Запись модерации не найдена")
        database.withTransaction {
            updateEntityStatus(queueItem, approved = false)
            moderationDao.updateDecision(queueId, "REJECTED", moderatorId, comment.trim())
            activityLogDao.insert(
                ActivityLogEntity(
                    userId = moderatorId,
                    actionType = ActivityActionType.REJECT_CONTENT.name,
                    entityType = queueItem.entityType,
                    entityId = queueItem.entityId
                )
            )
        }
        return RepositoryResult.success(queueItem.copy(status = "REJECTED", moderatorId = moderatorId, comment = comment.trim(), checkedAt = System.currentTimeMillis()))
    }

    suspend fun approvePlace(queueId: Long, placeId: Long, moderatorId: Long, comment: String = "") {
        approve(queueId, moderatorId, comment)
    }

    suspend fun rejectPlace(queueId: Long, placeId: Long, moderatorId: Long, comment: String) {
        reject(queueId, moderatorId, comment)
    }

    private suspend fun updateEntityStatus(queueItem: ModerationQueueEntity, approved: Boolean) {
        val contentStatus = if (approved) ContentStatus.PUBLISHED.name else ContentStatus.REJECTED.name
        when (queueItem.entityType) {
            ModerationEntityType.PLACE.name -> placeDao.updateStatus(queueItem.entityId, contentStatus)
            ModerationEntityType.PHOTO.name -> placePhotoDao.updateStatus(queueItem.entityId, contentStatus)
            ModerationEntityType.REVIEW.name -> reviewDao.updateStatus(queueItem.entityId, contentStatus)
            ModerationEntityType.ROUTE.name -> routeDao.updateStatus(queueItem.entityId, contentStatus)
            ModerationEntityType.REPORT.name -> reportDao.updateStatus(queueItem.entityId, if (approved) ReportStatus.RESOLVED.name else ReportStatus.REJECTED.name)
        }
    }

    private suspend fun resolveAuthorId(queueItem: ModerationQueueEntity): Long? = when (queueItem.entityType) {
        ModerationEntityType.PLACE.name -> placeDao.getById(queueItem.entityId)?.authorId
        ModerationEntityType.PHOTO.name -> placePhotoDao.getById(queueItem.entityId)?.userId
        ModerationEntityType.REVIEW.name -> reviewDao.getById(queueItem.entityId)?.userId
        ModerationEntityType.ROUTE.name -> routeDao.getById(queueItem.entityId)?.authorId
        else -> null
    }

    private fun pointsFor(entityType: String): Int = when (entityType) {
        ModerationEntityType.PLACE.name -> pointsManager.pointsForApprovedPlace()
        ModerationEntityType.PHOTO.name -> pointsManager.pointsForApprovedPhoto()
        ModerationEntityType.REVIEW.name -> pointsManager.pointsForApprovedReview()
        else -> 0
    }

    private fun conditionFor(entityType: String): String? = when (entityType) {
        ModerationEntityType.PLACE.name -> "PLACES_COUNT"
        ModerationEntityType.PHOTO.name -> "PHOTOS_COUNT"
        ModerationEntityType.REVIEW.name -> "REVIEWS_COUNT"
        else -> null
    }

    private suspend fun currentValueFor(userId: Long, entityType: String): Int = when (entityType) {
        ModerationEntityType.PLACE.name -> placeDao.countByAuthorAndStatus(userId, ContentStatus.PUBLISHED.name)
        ModerationEntityType.PHOTO.name -> placePhotoDao.countByUserAndStatus(userId, ContentStatus.PUBLISHED.name)
        ModerationEntityType.REVIEW.name -> reviewDao.countByUserAndStatus(userId, ContentStatus.PUBLISHED.name)
        else -> 0
    }

    private suspend fun addPoints(userId: Long, points: Int) {
        if (points == 0) return
        val user = userDao.getById(userId) ?: return
        val total = user.points + points
        userDao.updatePointsAndLevel(userId, total, pointsManager.calculateLevel(total))
    }

    private suspend fun unlockAchievements(userId: Long, conditionType: String, currentValue: Int): List<AchievementEntity> {
        val unlocked = mutableListOf<AchievementEntity>()
        achievementDao.getByCondition(conditionType).forEach { achievement ->
            val exists = achievementDao.findUserAchievement(userId, achievement.id) != null
            if (!exists && achievementChecker.shouldUnlock(conditionType, achievement.conditionValue, currentValue)) {
                achievementDao.insertUserAchievement(UserAchievementEntity(userId = userId, achievementId = achievement.id))
                activityLogDao.insert(
                    ActivityLogEntity(
                        userId = userId,
                        actionType = ActivityActionType.ACHIEVEMENT_UNLOCKED.name,
                        entityType = "ACHIEVEMENT",
                        entityId = achievement.id,
                        pointsChange = achievement.pointsReward
                    )
                )
                addPoints(userId, achievement.pointsReward)
                unlocked += achievement
            }
        }
        return unlocked
    }
}
