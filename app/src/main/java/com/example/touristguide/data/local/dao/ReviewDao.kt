package com.example.touristguide.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.touristguide.data.local.entity.ReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ReviewEntity): Long

    @Update
    suspend fun update(item: ReviewEntity)

    @Delete
    suspend fun delete(item: ReviewEntity)

    @Query("SELECT * FROM reviews WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ReviewEntity?

    @Query("SELECT * FROM reviews ORDER BY id DESC")
    fun observeAll(): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews WHERE place_id = :placeId AND status = 'PUBLISHED' ORDER BY created_at DESC")
    fun observeByPlace(placeId: Long): Flow<List<ReviewEntity>>

    @Query("SELECT COUNT(*) FROM reviews WHERE user_id = :userId AND status = :status")
    suspend fun countByUserAndStatus(userId: Long, status: String = "PUBLISHED"): Int

    @Query("UPDATE reviews SET status = :status WHERE id = :reviewId")
    suspend fun updateStatus(reviewId: Long, status: String)
}
