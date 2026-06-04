package com.example.touristguide.data.repository

import androidx.room.withTransaction
import com.example.touristguide.core.enums.ActivityActionType
import com.example.touristguide.core.enums.TaskStatus
import com.example.touristguide.core.gamification.PointsManager
import com.example.touristguide.core.gamification.WeeklyTaskManager
import com.example.touristguide.data.local.AppDatabase
import com.example.touristguide.data.local.dao.ActivityLogDao
import com.example.touristguide.data.local.dao.TaskDao
import com.example.touristguide.data.local.dao.UserDao
import com.example.touristguide.data.local.entity.ActivityLogEntity
import com.example.touristguide.data.local.entity.TaskEntity
import com.example.touristguide.data.local.entity.UserTaskEntity
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val database: AppDatabase,
    private val taskDao: TaskDao,
    private val userDao: UserDao,
    private val activityLogDao: ActivityLogDao,
    private val weeklyTaskManager: WeeklyTaskManager = WeeklyTaskManager(),
    private val pointsManager: PointsManager = PointsManager()
) {
    fun observeWeeklyTasks(): Flow<List<TaskEntity>> = taskDao.observeWeekly()

    fun observeUserTasks(userId: Long): Flow<List<UserTaskEntity>> = taskDao.observeUserTasks(userId)

    suspend fun ensureWeeklyTasksForUser(userId: Long) {
        taskDao.getWeekly().forEach { task ->
            taskDao.insertUserTask(UserTaskEntity(userId = userId, taskId = task.id))
        }
    }

    suspend fun progressTask(userId: Long, taskId: Long, increment: Int = 1): RepositoryResult<UserTaskEntity> {
        val task = taskDao.getById(taskId) ?: return RepositoryResult.failure("Задание не найдено")
        val existing = taskDao.findUserTask(userId, taskId)
        val row = existing ?: UserTaskEntity(userId = userId, taskId = taskId).let {
            it.copy(id = taskDao.insertUserTask(it))
        }
        if (row.status == TaskStatus.COMPLETED.name) return RepositoryResult.success(row)

        var updated = row
        database.withTransaction {
            val progress = (row.progress + increment).coerceAtMost(task.targetValue)
            val completed = weeklyTaskManager.isCompleted(progress, task.targetValue)
            val status = if (completed) TaskStatus.COMPLETED.name else TaskStatus.ACTIVE.name
            val completedAt = if (completed) System.currentTimeMillis() else null
            taskDao.updateUserTaskProgress(row.id, progress, status, completedAt)
            updated = row.copy(progress = progress, status = status, completedAt = completedAt)

            if (completed) {
                val user = userDao.getById(userId)
                if (user != null) {
                    val total = user.points + task.pointsReward
                    userDao.updatePointsAndLevel(userId, total, pointsManager.calculateLevel(total))
                }
                activityLogDao.insert(
                    ActivityLogEntity(
                        userId = userId,
                        actionType = ActivityActionType.TASK_COMPLETED.name,
                        entityType = "TASK",
                        entityId = taskId,
                        pointsChange = task.pointsReward
                    )
                )
            }
        }
        return RepositoryResult.success(updated)
    }
}
