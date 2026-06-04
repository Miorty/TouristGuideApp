package com.example.touristguide.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.touristguide.data.local.entity.PlaceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PlaceEntity): Long

    @Update
    suspend fun update(item: PlaceEntity)

    @Delete
    suspend fun delete(item: PlaceEntity)

    @Query("SELECT * FROM places WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): PlaceEntity?

    @Query("SELECT * FROM places WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<PlaceEntity?>

    @Query("SELECT * FROM places ORDER BY id DESC")
    fun observeAll(): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM places WHERE status = :status ORDER BY created_at DESC")
    fun observePlacesByStatus(status: String = "PUBLISHED"): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM places WHERE status = 'PUBLISHED' AND (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%') ORDER BY title")
    fun searchPlaces(query: String): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM places WHERE category_id = :categoryId AND status = 'PUBLISHED' ORDER BY title")
    fun observeByCategory(categoryId: Long): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM places WHERE author_id = :userId ORDER BY created_at DESC")
    fun observeByAuthor(userId: Long): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM places WHERE status IN (:statuses) ORDER BY created_at DESC")
    suspend fun getByStatuses(statuses: List<String>): List<PlaceEntity>

    @Query("SELECT * FROM places WHERE ABS(latitude - :latitude) < :delta AND ABS(longitude - :longitude) < :delta")
    suspend fun findNearby(latitude: Double, longitude: Double, delta: Double = 0.0005): List<PlaceEntity>

    @Query("SELECT COUNT(*) FROM places WHERE author_id = :userId AND status = :status")
    suspend fun countByAuthorAndStatus(userId: Long, status: String = "PUBLISHED"): Int

    @Query("UPDATE places SET status = :status, updated_at = :updatedAt WHERE id = :placeId")
    suspend fun updateStatus(placeId: Long, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE places SET average_rating = :averageRating, updated_at = :updatedAt WHERE id = :placeId")
    suspend fun updateAverageRating(placeId: Long, averageRating: Double, updatedAt: Long = System.currentTimeMillis())
}
