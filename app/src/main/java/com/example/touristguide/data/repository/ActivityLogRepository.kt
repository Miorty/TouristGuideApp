package com.example.touristguide.data.repository

import com.example.touristguide.data.local.dao.ActivityLogDao
import com.example.touristguide.data.local.entity.ActivityLogEntity
import kotlinx.coroutines.flow.Flow

class ActivityLogRepository(private val activityLogDao: ActivityLogDao) {
    fun observeFeed(): Flow<List<ActivityLogEntity>> = activityLogDao.observeAll()

    fun observeUserActivity(userId: Long): Flow<List<ActivityLogEntity>> = activityLogDao.observeByUser(userId)

    suspend fun log(
        userId: Long,
        actionType: String,
        entityType: String,
        entityId: Long? = null,
        pointsChange: Int = 0
    ): Long = activityLogDao.insert(
        ActivityLogEntity(
            userId = userId,
            actionType = actionType,
            entityType = entityType,
            entityId = entityId,
            pointsChange = pointsChange
        )
    )
}
