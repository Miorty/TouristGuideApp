package com.example.touristguide.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.touristguide.data.local.entity.ActivityLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ActivityLogEntity): Long

    @Update
    suspend fun update(item: ActivityLogEntity)

    @Delete
    suspend fun delete(item: ActivityLogEntity)

    @Query("SELECT * FROM activity_log WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ActivityLogEntity?

    @Query("SELECT * FROM activity_log ORDER BY id DESC")
    fun observeAll(): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_log WHERE user_id = :userId ORDER BY created_at DESC")
    fun observeByUser(userId: Long): Flow<List<ActivityLogEntity>>

    @Query("SELECT COUNT(*) FROM activity_log WHERE user_id = :userId AND action_type = :actionType")
    suspend fun countByUserAndAction(userId: Long, actionType: String): Int
}
