package com.example.touristguide.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.touristguide.data.local.entity.ReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ReportEntity): Long

    @Update
    suspend fun update(item: ReportEntity)

    @Delete
    suspend fun delete(item: ReportEntity)

    @Query("SELECT * FROM reports WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ReportEntity?

    @Query("SELECT * FROM reports ORDER BY id DESC")
    fun observeAll(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE status = :status ORDER BY created_at DESC")
    fun observeByStatus(status: String = "NEW"): Flow<List<ReportEntity>>

    @Query("UPDATE reports SET status = :status WHERE id = :reportId")
    suspend fun updateStatus(reportId: Long, status: String)
}
