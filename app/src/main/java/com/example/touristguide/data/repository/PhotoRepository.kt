package com.example.touristguide.data.repository

import androidx.room.withTransaction
import com.example.touristguide.core.enums.ActivityActionType
import com.example.touristguide.core.enums.ContentStatus
import com.example.touristguide.core.enums.ModerationEntityType
import com.example.touristguide.core.validation.PhotoValidator
import com.example.touristguide.data.local.AppDatabase
import com.example.touristguide.data.local.dao.ActivityLogDao
import com.example.touristguide.data.local.dao.ModerationDao
import com.example.touristguide.data.local.dao.PlacePhotoDao
import com.example.touristguide.data.local.entity.ActivityLogEntity
import com.example.touristguide.data.local.entity.ModerationQueueEntity
import com.example.touristguide.data.local.entity.PlacePhotoEntity
import kotlinx.coroutines.flow.Flow

class PhotoRepository(
    private val database: AppDatabase,
    private val placePhotoDao: PlacePhotoDao,
    private val moderationDao: ModerationDao,
    private val activityLogDao: ActivityLogDao,
    private val validator: PhotoValidator = PhotoValidator()
) {
    fun observePlacePhotos(placeId: Long): Flow<List<PlacePhotoEntity>> = placePhotoDao.observeByPlace(placeId)

    suspend fun addPhoto(placeId: Long, userId: Long, filePath: String?): RepositoryResult<PlacePhotoEntity> {
        val result = validator.validate(filePath)
        if (!result.isValid) return RepositoryResult.failure(result.message ?: "Некорректное фото")

        val saved = database.withTransaction {
            val photo = PlacePhotoEntity(
                placeId = placeId,
                userId = userId,
                filePath = filePath.orEmpty(),
                status = ContentStatus.PENDING.name
            )
            val photoId = placePhotoDao.insert(photo)
            moderationDao.insert(ModerationQueueEntity(entityType = ModerationEntityType.PHOTO.name, entityId = photoId))
            activityLogDao.insert(
                ActivityLogEntity(
                    userId = userId,
                    actionType = ActivityActionType.ADD_PHOTO.name,
                    entityType = ModerationEntityType.PHOTO.name,
                    entityId = photoId
                )
            )
            photo.copy(id = photoId)
        }
        return RepositoryResult.success(saved)
    }
}
