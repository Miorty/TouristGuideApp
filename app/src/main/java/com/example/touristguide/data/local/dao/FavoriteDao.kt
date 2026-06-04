package com.example.touristguide.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.touristguide.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: FavoriteEntity): Long

    @Update
    suspend fun update(item: FavoriteEntity)

    @Delete
    suspend fun delete(item: FavoriteEntity)

    @Query("SELECT * FROM favorites WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): FavoriteEntity?

    @Query("SELECT * FROM favorites ORDER BY id DESC")
    fun observeAll(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE user_id = :userId ORDER BY created_at DESC")
    fun observeByUser(userId: Long): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE user_id = :userId ORDER BY created_at DESC")
    suspend fun getByUser(userId: Long): List<FavoriteEntity>

    @Query("SELECT * FROM favorites WHERE user_id = :userId AND place_id = :placeId LIMIT 1")
    suspend fun findFavorite(userId: Long, placeId: Long): FavoriteEntity?

    @Query("DELETE FROM favorites WHERE user_id = :userId AND place_id = :placeId")
    suspend fun deleteByUserAndPlace(userId: Long, placeId: Long)
}
