package com.example.touristguide.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.touristguide.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: UserEntity): Long

    @Update
    suspend fun update(item: UserEntity)

    @Delete
    suspend fun delete(item: UserEntity)

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): UserEntity?

    @Query("SELECT * FROM users ORDER BY id DESC")
    fun observeAll(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<UserEntity?>

    @Query("UPDATE users SET points = points + :points WHERE id = :userId")
    suspend fun addPoints(userId: Long, points: Int)

    @Query("UPDATE users SET points = :points, level = :level WHERE id = :userId")
    suspend fun updatePointsAndLevel(userId: Long, points: Int, level: Int)
}
