package com.example.touristguide.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.touristguide.data.local.entity.AchievementEntity
import com.example.touristguide.data.local.entity.UserAchievementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: AchievementEntity): Long

    @Update
    suspend fun update(item: AchievementEntity)

    @Delete
    suspend fun delete(item: AchievementEntity)

    @Query("SELECT * FROM achievements WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): AchievementEntity?

    @Query("SELECT * FROM achievements ORDER BY id DESC")
    fun observeAll(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE condition_type = :conditionType ORDER BY condition_value")
    suspend fun getByCondition(conditionType: String): List<AchievementEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUserAchievement(item: UserAchievementEntity): Long

    @Query("SELECT achievement_id FROM user_achievements WHERE user_id = :userId")
    suspend fun getUnlockedAchievementIds(userId: Long): List<Long>

    @Query("SELECT * FROM user_achievements WHERE user_id = :userId AND achievement_id = :achievementId LIMIT 1")
    suspend fun findUserAchievement(userId: Long, achievementId: Long): UserAchievementEntity?

    @Query("SELECT achievements.* FROM achievements INNER JOIN user_achievements ON achievements.id = user_achievements.achievement_id WHERE user_achievements.user_id = :userId ORDER BY user_achievements.received_at DESC")
    fun observeByUser(userId: Long): Flow<List<AchievementEntity>>
}
