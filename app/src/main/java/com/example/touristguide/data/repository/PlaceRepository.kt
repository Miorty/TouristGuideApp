package com.example.touristguide.data.repository

import androidx.room.withTransaction
import com.example.touristguide.core.enums.ActivityActionType
import com.example.touristguide.core.enums.ContentStatus
import com.example.touristguide.core.enums.ModerationEntityType
import com.example.touristguide.core.validation.DuplicateChecker
import com.example.touristguide.core.validation.PlaceValidator
import com.example.touristguide.data.local.AppDatabase
import com.example.touristguide.data.local.dao.ActivityLogDao
import com.example.touristguide.data.local.dao.ModerationDao
import com.example.touristguide.data.local.dao.PlaceDao
import com.example.touristguide.data.local.entity.ActivityLogEntity
import com.example.touristguide.data.local.entity.ModerationQueueEntity
import com.example.touristguide.data.local.entity.PlaceEntity
import kotlinx.coroutines.flow.Flow

class PlaceRepository(
    private val database: AppDatabase,
    private val placeDao: PlaceDao,
    private val moderationDao: ModerationDao,
    private val activityLogDao: ActivityLogDao,
    private val validator: PlaceValidator = PlaceValidator(),
    private val duplicateChecker: DuplicateChecker = DuplicateChecker()
) {
    fun observePublishedPlaces(): Flow<List<PlaceEntity>> = placeDao.observePlacesByStatus(ContentStatus.PUBLISHED.name)

    fun observePlace(placeId: Long): Flow<PlaceEntity?> = placeDao.observeById(placeId)

    fun searchPlaces(query: String): Flow<List<PlaceEntity>> = placeDao.searchPlaces(query)

    fun observeByCategory(categoryId: Long): Flow<List<PlaceEntity>> = placeDao.observeByCategory(categoryId)

    fun observeUserPlaces(userId: Long): Flow<List<PlaceEntity>> = placeDao.observeByAuthor(userId)

    suspend fun getPlace(placeId: Long): PlaceEntity? = placeDao.getById(placeId)

    suspend fun addUserPlace(place: PlaceEntity): RepositoryResult<PlaceEntity> {
        val result = validator.validate(place.title, place.description, place.latitude, place.longitude)
        if (!result.isValid) return RepositoryResult.failure(result.message ?: "Некорректные данные места")

        val nearby = placeDao.findNearby(place.latitude, place.longitude)
        val isDuplicate = nearby.any { existing ->
            duplicateChecker.isPossibleDuplicate(
                place.title,
                place.latitude,
                place.longitude,
                existing.title,
                existing.latitude,
                existing.longitude
            )
        }
        val contentStatus = if (isDuplicate) ContentStatus.POSSIBLE_DUPLICATE.name else ContentStatus.PENDING.name

        val saved = database.withTransaction {
            val placeId = placeDao.insert(place.copy(status = contentStatus))
            moderationDao.insert(ModerationQueueEntity(entityType = ModerationEntityType.PLACE.name, entityId = placeId))
            activityLogDao.insert(
                ActivityLogEntity(
                    userId = place.authorId,
                    actionType = ActivityActionType.ADD_PLACE.name,
                    entityType = ModerationEntityType.PLACE.name,
                    entityId = placeId
                )
            )
            place.copy(id = placeId, status = contentStatus)
        }
        return RepositoryResult.success(saved)
    }
}
