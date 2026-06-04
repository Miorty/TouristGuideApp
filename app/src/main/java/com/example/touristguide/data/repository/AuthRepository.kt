package com.example.touristguide.data.repository

import com.example.touristguide.core.enums.ActivityActionType
import com.example.touristguide.core.enums.UserRole
import com.example.touristguide.data.local.dao.ActivityLogDao
import com.example.touristguide.data.local.dao.UserDao
import com.example.touristguide.data.local.entity.ActivityLogEntity
import com.example.touristguide.data.local.entity.UserEntity

class AuthRepository(
    private val userDao: UserDao,
    private val activityLogDao: ActivityLogDao
) {
    suspend fun loginAsGuest(): Long {
        val existing = userDao.findByEmail("guest@local")
        val userId = existing?.id ?: userDao.insert(UserEntity(username = "Иван Иванов", email = "guest@local", password = "1234"))
        activityLogDao.insert(ActivityLogEntity(userId = userId, actionType = ActivityActionType.LOGIN.name, entityType = "USER", entityId = userId))
        return userId
    }

    suspend fun loginByEmail(email: String, password: String): RepositoryResult<UserEntity> {
        val user = userDao.findByEmail(email.trim())
        return when {
            user == null -> RepositoryResult.failure("Пользователь с таким email не найден")
            user.password != password -> RepositoryResult.failure("Неверный пароль")
            else -> {
                activityLogDao.insert(ActivityLogEntity(userId = user.id, actionType = ActivityActionType.LOGIN.name, entityType = "USER", entityId = user.id))
                RepositoryResult.success(user)
            }
        }
    }

    suspend fun register(username: String, email: String, password: String): RepositoryResult<UserEntity> {
        val cleanEmail = email.trim()
        if (username.isBlank()) return RepositoryResult.failure("Введите имя")
        if (!cleanEmail.contains("@")) return RepositoryResult.failure("Введите корректный email")
        if (password.length < 4) return RepositoryResult.failure("Пароль должен быть не короче 4 символов")
        if (userDao.findByEmail(cleanEmail) != null) return RepositoryResult.failure("Такой email уже зарегистрирован")

        val user = UserEntity(username = username.trim(), email = cleanEmail, password = password, role = UserRole.USER.name)
        val id = userDao.insert(user)
        val saved = user.copy(id = id)
        activityLogDao.insert(ActivityLogEntity(userId = id, actionType = ActivityActionType.LOGIN.name, entityType = "USER", entityId = id))
        return RepositoryResult.success(saved)
    }
}
