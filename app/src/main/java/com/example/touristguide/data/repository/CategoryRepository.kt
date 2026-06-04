package com.example.touristguide.data.repository

import com.example.touristguide.data.local.dao.CategoryDao
import com.example.touristguide.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val categoryDao: CategoryDao) {
    fun observeCategories(): Flow<List<CategoryEntity>> = categoryDao.observeAll()

    suspend fun getCategory(id: Long): CategoryEntity? = categoryDao.getById(id)

    suspend fun ensureCategory(name: String, description: String = ""): Long {
        val existing = categoryDao.findByName(name)
        return existing?.id ?: categoryDao.insert(CategoryEntity(name = name, description = description))
    }
}
