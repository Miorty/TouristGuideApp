package com.example.touristguide.data.repository

import androidx.room.withTransaction
import com.example.touristguide.core.enums.ActivityActionType
import com.example.touristguide.core.gamification.AchievementChecker
import com.example.touristguide.core.gamification.PointsManager
import com.example.touristguide.data.local.AppDatabase
import com.example.touristguide.data.local.dao.AchievementDao
import com.example.touristguide.data.local.dao.ActivityLogDao
import com.example.touristguide.data.local.dao.UserDao
import com.example.touristguide.data.local.entity.AchievementEntity
import com.example.touristguide.data.local.entity.ActivityLogEntity
import com.example.touristguide.data.local.entity.UserAchievementEntity
import kotlinx.coroutines.flow.Flow

class AchievementRepository(
    private val database: AppDatabase,
    private val achievementDao: AchievementDao,
    private val userDao: UserDao,
    private val activityLogDao: ActivityLogDao,
    private val achievementChecker: AchievementChecker = AchievementChecker(),
    private val pointsManager: PointsManager = PointsManager()
) {
    fun observeAll(): Flow<List<AchievementEntity>> = achievementDao.observeAll()

    fun observeUserAchievements(userId: Long): Flow<List<AchievementEntity>> = achievementDao.observeByUser(userId)

    suspend fun checkAndUnlock(userId: Long, conditionType: String, currentValue: Int): List<AchievementEntity> {
        val candidates = achievementDao.getByCondition(conditionType)
        if (candidates.isEmpty()) return emptyList()

        val user = userDao.getById(userId) ?: return emptyList()
        val unlocked = mutableListOf<AchievementEntity>()
        database.withTransaction {
            var totalPoints = user.points
            candidates.forEach { achievement ->
                val alreadyUnlocked = achievementDao.findUserAchievement(userId, achievement.id) != null
                if (!alreadyUnlocked && achievementChecker.shouldUnlock(achievement.conditionType, achievement.conditionValue, currentValue)) {
                    achievementDao.insertUserAchievement(UserAchievementEntity(userId = userId, achievementId = achievement.id))
                    activityLogDao.insert(
                        ActivityLogEntity(
                            userId = userId,
                            actionType = ActivityActionType.ACHIEVEMENT_UNLOCKED.name,
                            entityType = "ACHIEVEMENT",
                            entityId = achievement.id,
                            pointsChange = achievement.pointsReward
                        )
                    )
                    totalPoints += achievement.pointsReward
                    unlocked += achievement
                }
            }
            if (totalPoints != user.points) {
                userDao.updatePointsAndLevel(userId, totalPoints, pointsManager.calculateLevel(totalPoints))
            }
        }
        return unlocked
    }
}
