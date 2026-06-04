package com.example.touristguide

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.touristguide.data.local.AppDatabase
import com.example.touristguide.data.local.entity.CategoryEntity
import com.example.touristguide.data.local.entity.PlaceEntity
import com.example.touristguide.data.local.entity.UserEntity
import com.example.touristguide.data.repository.PlaceRepository
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlaceFlowInstrumentedTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: PlaceRepository

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = PlaceRepository(database, database.placeDao(), database.moderationDao(), database.activityLogDao())
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun addPlaceCreatesPendingPlaceAndModerationQueueItem() = runTest {
        val userId = database.userDao().insert(UserEntity(username = "Автор", email = "author@local", password = "1234"))
        val categoryId = database.categoryDao().insert(CategoryEntity(name = "Музеи"))

        val result = repository.addUserPlace(
            PlaceEntity(
                categoryId = categoryId,
                authorId = userId,
                title = "Новый музей",
                description = "Подробное описание нового культурного места для туристического маршрута.",
                latitude = 58.02,
                longitude = 56.25,
                address = "Пермь"
            )
        )

        assertTrue(result.isSuccess)
        val place = result.data
        assertNotNull(place)
        assertEquals("PENDING", place?.status)
        assertNotNull(database.moderationDao().findByEntity("PLACE", place?.id ?: 0))
    }
}
