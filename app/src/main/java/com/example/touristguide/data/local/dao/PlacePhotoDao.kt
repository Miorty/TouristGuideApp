package com.example.touristguide.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.touristguide.data.local.entity.PlacePhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlacePhotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PlacePhotoEntity): Long

    @Update
    suspend fun update(item: PlacePhotoEntity)

    @Delete
    suspend fun delete(item: PlacePhotoEntity)

    @Query("SELECT * FROM place_photos WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): PlacePhotoEntity?

    @Query("SELECT * FROM place_photos ORDER BY id DESC")
    fun observeAll(): Flow<List<PlacePhotoEntity>>

    @Query("SELECT * FROM place_photos WHERE place_id = :placeId AND status = :status ORDER BY created_at DESC")
    fun observeByPlace(placeId: Long, status: String = "PUBLISHED"): Flow<List<PlacePhotoEntity>>

    @Query("SELECT COUNT(*) FROM place_photos WHERE user_id = :userId AND status = :status")
    suspend fun countByUserAndStatus(userId: Long, status: String = "PUBLISHED"): Int

    @Query("UPDATE place_photos SET status = :status WHERE id = :photoId")
    suspend fun updateStatus(photoId: Long, status: String)
}
