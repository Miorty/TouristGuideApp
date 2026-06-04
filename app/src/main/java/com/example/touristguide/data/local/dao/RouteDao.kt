package com.example.touristguide.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.touristguide.data.local.entity.RouteEntity
import com.example.touristguide.data.local.entity.RoutePlaceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: RouteEntity): Long

    @Update
    suspend fun update(item: RouteEntity)

    @Delete
    suspend fun delete(item: RouteEntity)

    @Query("SELECT * FROM routes WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): RouteEntity?

    @Query("SELECT * FROM routes ORDER BY id DESC")
    fun observeAll(): Flow<List<RouteEntity>>

    @Query("SELECT * FROM routes WHERE status = 'PUBLISHED' ORDER BY id")
    fun observePublishedRoutes(): Flow<List<RouteEntity>>

    @Query("SELECT * FROM routes WHERE author_id = :userId ORDER BY created_at DESC")
    fun observeByAuthor(userId: Long): Flow<List<RouteEntity>>

    @Query("SELECT * FROM route_places WHERE route_id = :routeId ORDER BY position")
    suspend fun getRoutePlaces(routeId: Long): List<RoutePlaceEntity>

    @Query("SELECT * FROM route_places WHERE route_id = :routeId ORDER BY position")
    fun observeRoutePlaces(routeId: Long): Flow<List<RoutePlaceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutePlace(routePlace: RoutePlaceEntity): Long

    @Query("DELETE FROM route_places WHERE route_id = :routeId")
    suspend fun deleteRoutePlaces(routeId: Long)

    @Query("UPDATE routes SET status = :status WHERE id = :routeId")
    suspend fun updateStatus(routeId: Long, status: String)
}
