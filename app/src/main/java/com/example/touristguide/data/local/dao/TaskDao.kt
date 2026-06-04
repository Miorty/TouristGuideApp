package com.example.touristguide.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.touristguide.data.local.entity.TaskEntity
import com.example.touristguide.data.local.entity.UserTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TaskEntity): Long

    @Update
    suspend fun update(item: TaskEntity)

    @Delete
    suspend fun delete(item: TaskEntity)

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun observeAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE is_weekly = 1 ORDER BY id DESC")
    fun observeWeekly(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE is_weekly = 1 ORDER BY id DESC")
    suspend fun getWeekly(): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUserTask(item: UserTaskEntity): Long

    @Query("SELECT * FROM user_tasks WHERE user_id = :userId AND task_id = :taskId LIMIT 1")
    suspend fun findUserTask(userId: Long, taskId: Long): UserTaskEntity?

    @Query("SELECT * FROM user_tasks WHERE user_id = :userId ORDER BY id DESC")
    fun observeUserTasks(userId: Long): Flow<List<UserTaskEntity>>

    @Query("UPDATE user_tasks SET progress = :progress, status = :status, completed_at = :completedAt WHERE id = :id")
    suspend fun updateUserTaskProgress(id: Long, progress: Int, status: String, completedAt: Long? = null)
}
