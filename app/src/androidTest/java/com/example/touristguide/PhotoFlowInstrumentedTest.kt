package com.example.touristguide

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.touristguide.data.local.AppDatabase
import com.example.touristguide.data.local.entity.CategoryEntity
import com.example.touristguide.data.local.entity.PlaceEntity
import com.example.touristguide.data.local.entity.UserEntity
import com.example.touristguide.data.repository.PhotoRepository
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PhotoFlowInstrumentedTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: PhotoRepository

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = PhotoRepository(database, database.placePhotoDao(), database.moderationDao(), database.activityLogDao())
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun addPhotoCreatesPendingPhotoAndModerationQueueItem() = runTest {
        val userId = database.userDao().insert(UserEntity(username = "Автор", email = "photo@author", password = "1234"))
        val categoryId = database.categoryDao().insert(CategoryEntity(name = "Памятники"))
        val placeId = database.placeDao().insert(
            PlaceEntity(
                categoryId = categoryId,
                authorId = userId,
                title = "Место с фото",
                description = "Подробное описание места, к которому пользователь добавляет фотографию.",
                latitude = 58.01,
                longitude = 56.24,
                status = "PUBLISHED"
            )
        )

        val result = repository.addPhoto(placeId, userId, "/local/photo.jpg")

        assertTrue(result.isSuccess)
        val photo = result.data
        assertNotNull(photo)
        assertEquals("PENDING", photo?.status)
        assertNotNull(database.moderationDao().findByEntity("PHOTO", photo?.id ?: 0))
    }
}
