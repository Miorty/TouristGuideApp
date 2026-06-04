package com.example.touristguide.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.touristguide.data.local.entity.RatingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RatingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: RatingEntity): Long

    @Update
    suspend fun update(item: RatingEntity)

    @Delete
    suspend fun delete(item: RatingEntity)

    @Query("SELECT * FROM ratings WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): RatingEntity?

    @Query("SELECT * FROM ratings ORDER BY id DESC")
    fun observeAll(): Flow<List<RatingEntity>>

    @Query("SELECT AVG(value) FROM ratings WHERE place_id = :placeId")
    suspend fun getAverageRating(placeId: Long): Double?

    @Query("SELECT * FROM ratings WHERE user_id = :userId AND place_id = :placeId LIMIT 1")
    suspend fun findUserRating(userId: Long, placeId: Long): RatingEntity?

    @Query("UPDATE ratings SET value = :value, created_at = :updatedAt WHERE id = :ratingId")
    suspend fun updateValue(ratingId: Long, value: Int, updatedAt: Long = System.currentTimeMillis())
}
