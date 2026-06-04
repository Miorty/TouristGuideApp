package com.example.touristguide.data.repository

import com.example.touristguide.core.gamification.PointsManager
import com.example.touristguide.data.local.dao.UserDao
import com.example.touristguide.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

class UserRepository(
    private val userDao: UserDao,
    private val pointsManager: PointsManager = PointsManager()
) {
    fun observeUser(userId: Long): Flow<UserEntity?> = userDao.observeById(userId)

    suspend fun getUser(userId: Long): UserEntity? = userDao.getById(userId)

    suspend fun addPoints(userId: Long, points: Int): RepositoryResult<UserEntity> {
        val user = userDao.getById(userId) ?: return RepositoryResult.failure("Пользователь не найден")
        val total = user.points + points
        val level = pointsManager.calculateLevel(total)
        userDao.updatePointsAndLevel(userId, total, level)
        return RepositoryResult.success(user.copy(points = total, level = level))
    }
}
