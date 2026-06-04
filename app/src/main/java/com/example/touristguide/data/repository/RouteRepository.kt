package com.example.touristguide.data.repository

import androidx.room.withTransaction
import com.example.touristguide.core.enums.ContentStatus
import com.example.touristguide.data.local.AppDatabase
import com.example.touristguide.data.local.dao.PlaceDao
import com.example.touristguide.data.local.dao.RouteDao
import com.example.touristguide.data.local.entity.PlaceEntity
import com.example.touristguide.data.local.entity.RouteEntity
import com.example.touristguide.data.local.entity.RoutePlaceEntity
import kotlinx.coroutines.flow.Flow

class RouteRepository(
    private val database: AppDatabase,
    private val routeDao: RouteDao,
    private val placeDao: PlaceDao
) {
    fun observePublishedRoutes(): Flow<List<RouteEntity>> = routeDao.observePublishedRoutes()

    fun observeUserRoutes(userId: Long): Flow<List<RouteEntity>> = routeDao.observeByAuthor(userId)

    suspend fun getRoute(routeId: Long): RouteEntity? = routeDao.getById(routeId)

    suspend fun getRoutePlaces(routeId: Long): List<PlaceEntity> =
        routeDao.getRoutePlaces(routeId).mapNotNull { routePlace -> placeDao.getById(routePlace.placeId) }

    suspend fun createRoute(
        authorId: Long,
        title: String,
        description: String,
        durationMinutes: Int,
        distanceKm: Double,
        placeIds: List<Long>
    ): RepositoryResult<RouteEntity> {
        if (title.isBlank()) return RepositoryResult.failure("Введите название маршрута")
        if (placeIds.size < 2) return RepositoryResult.failure("Маршрут должен содержать минимум две точки")

        val saved = database.withTransaction {
            val route = RouteEntity(
                authorId = authorId,
                title = title.trim(),
                description = description.trim(),
                durationMinutes = durationMinutes,
                distanceKm = distanceKm,
                status = ContentStatus.PUBLISHED.name
            )
            val routeId = routeDao.insert(route)
            placeIds.forEachIndexed { index, placeId ->
                routeDao.insertRoutePlace(RoutePlaceEntity(routeId = routeId, placeId = placeId, position = index + 1))
            }
            route.copy(id = routeId)
        }
        return RepositoryResult.success(saved)
    }
}
