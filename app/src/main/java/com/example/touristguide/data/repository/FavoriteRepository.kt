package com.example.touristguide.data.repository

import androidx.room.withTransaction
import com.example.touristguide.core.enums.ActivityActionType
import com.example.touristguide.core.enums.ModerationEntityType
import com.example.touristguide.data.local.AppDatabase
import com.example.touristguide.data.local.dao.ActivityLogDao
import com.example.touristguide.data.local.dao.FavoriteDao
import com.example.touristguide.data.local.dao.PlaceDao
import com.example.touristguide.data.local.entity.ActivityLogEntity
import com.example.touristguide.data.local.entity.FavoriteEntity
import com.example.touristguide.data.local.entity.PlaceEntity
import kotlinx.coroutines.flow.Flow

class FavoriteRepository(
    private val database: AppDatabase,
    private val favoriteDao: FavoriteDao,
    private val placeDao: PlaceDao,
    private val activityLogDao: ActivityLogDao
) {
    fun observeUserFavorites(userId: Long): Flow<List<FavoriteEntity>> = favoriteDao.observeByUser(userId)

    suspend fun getFavoritePlaces(userId: Long): List<PlaceEntity> =
        favoriteDao.getByUser(userId).mapNotNull { favorite -> placeDao.getById(favorite.placeId) }

    suspend fun isFavorite(userId: Long, placeId: Long): Boolean =
        favoriteDao.findFavorite(userId, placeId) != null

    suspend fun toggleFavorite(userId: Long, placeId: Long): RepositoryResult<Boolean> {
        val place = placeDao.getById(placeId) ?: return RepositoryResult.failure("Место не найдено")
        val nowFavorite = database.withTransaction {
            val existing = favoriteDao.findFavorite(userId, placeId)
            if (existing == null) {
                favoriteDao.insert(FavoriteEntity(userId = userId, placeId = placeId))
                activityLogDao.insert(
                    ActivityLogEntity(
                        userId = userId,
                        actionType = ActivityActionType.ADD_FAVORITE.name,
                        entityType = ModerationEntityType.PLACE.name,
                        entityId = place.id
                    )
                )
                true
            } else {
                favoriteDao.deleteByUserAndPlace(userId, placeId)
                activityLogDao.insert(
                    ActivityLogEntity(
                        userId = userId,
                        actionType = ActivityActionType.REMOVE_FAVORITE.name,
                        entityType = ModerationEntityType.PLACE.name,
                        entityId = place.id
                    )
                )
                false
            }
        }
        return RepositoryResult.success(nowFavorite)
    }
}
