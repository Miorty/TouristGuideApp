package com.example.touristguide.data.repository

import androidx.room.withTransaction
import com.example.touristguide.core.enums.ActivityActionType
import com.example.touristguide.core.enums.ModerationEntityType
import com.example.touristguide.core.enums.ReportStatus
import com.example.touristguide.core.enums.ReportType
import com.example.touristguide.data.local.AppDatabase
import com.example.touristguide.data.local.dao.ActivityLogDao
import com.example.touristguide.data.local.dao.ModerationDao
import com.example.touristguide.data.local.dao.ReportDao
import com.example.touristguide.data.local.entity.ActivityLogEntity
import com.example.touristguide.data.local.entity.ModerationQueueEntity
import com.example.touristguide.data.local.entity.ReportEntity
import kotlinx.coroutines.flow.Flow

class ReportRepository(
    private val database: AppDatabase,
    private val reportDao: ReportDao,
    private val moderationDao: ModerationDao,
    private val activityLogDao: ActivityLogDao
) {
    fun observeNewReports(): Flow<List<ReportEntity>> = reportDao.observeByStatus(ReportStatus.NEW.name)

    suspend fun createReport(
        userId: Long,
        reportType: ReportType,
        comment: String,
        placeId: Long? = null,
        reviewId: Long? = null,
        photoId: Long? = null
    ): RepositoryResult<ReportEntity> {
        val targets = listOf(placeId, reviewId, photoId).count { it != null }
        if (targets != 1) return RepositoryResult.failure("Жалоба должна быть связана ровно с одним материалом")
        if (comment.isBlank()) return RepositoryResult.failure("Добавьте комментарий к жалобе")

        val saved = database.withTransaction {
            val report = ReportEntity(
                userId = userId,
                placeId = placeId,
                reviewId = reviewId,
                photoId = photoId,
                reportType = reportType.name,
                comment = comment.trim(),
                status = ReportStatus.NEW.name
            )
            val reportId = reportDao.insert(report)
            moderationDao.insert(ModerationQueueEntity(entityType = ModerationEntityType.REPORT.name, entityId = reportId))
            activityLogDao.insert(
                ActivityLogEntity(
                    userId = userId,
                    actionType = ActivityActionType.REPORT.name,
                    entityType = ModerationEntityType.REPORT.name,
                    entityId = reportId
                )
            )
            report.copy(id = reportId)
        }
        return RepositoryResult.success(saved)
    }
}
