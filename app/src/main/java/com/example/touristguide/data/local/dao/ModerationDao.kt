package com.example.touristguide.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.touristguide.data.local.entity.ModerationQueueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ModerationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ModerationQueueEntity): Long

    @Update
    suspend fun update(item: ModerationQueueEntity)

    @Delete
    suspend fun delete(item: ModerationQueueEntity)

    @Query("SELECT * FROM moderation_queue WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ModerationQueueEntity?

    @Query("SELECT * FROM moderation_queue ORDER BY id DESC")
    fun observeAll(): Flow<List<ModerationQueueEntity>>

    @Query("SELECT * FROM moderation_queue WHERE status = 'PENDING' ORDER BY created_at DESC")
    fun observePending(): Flow<List<ModerationQueueEntity>>

    @Query("SELECT * FROM moderation_queue WHERE entity_type = :entityType AND entity_id = :entityId ORDER BY created_at DESC LIMIT 1")
    suspend fun findByEntity(entityType: String, entityId: Long): ModerationQueueEntity?

    @Query("UPDATE moderation_queue SET status = :status, moderator_id = :moderatorId, comment = :comment, checked_at = :checkedAt WHERE id = :id")
    suspend fun updateDecision(id: Long, status: String, moderatorId: Long, comment: String, checkedAt: Long = System.currentTimeMillis())
}
