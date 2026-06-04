package com.example.touristguide

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.touristguide.data.local.AppDatabase
import com.example.touristguide.data.local.entity.CategoryEntity
import com.example.touristguide.data.local.entity.ModerationQueueEntity
import com.example.touristguide.data.local.entity.PlaceEntity
import com.example.touristguide.data.local.entity.UserEntity
import com.example.touristguide.data.repository.ModerationRepository
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ModerationFlowInstrumentedTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: ModerationRepository

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ModerationRepository(
            database = database,
            moderationDao = database.moderationDao(),
            placeDao = database.placeDao(),
            placePhotoDao = database.placePhotoDao(),
            reviewDao = database.reviewDao(),
            reportDao = database.reportDao(),
            routeDao = database.routeDao(),
            userDao = database.userDao(),
            achievementDao = database.achievementDao(),
            activityLogDao = database.activityLogDao()
        )
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun approvePlacePublishesPlaceAndAddsPoints() = runTest {
        val moderatorId = database.userDao().insert(UserEntity(username = "Модератор", email = "moderator@test", password = "1234", role = "MODERATOR"))
        val authorId = database.userDao().insert(UserEntity(username = "Автор", email = "author@test", password = "1234"))
        val categoryId = database.categoryDao().insert(CategoryEntity(name = "Памятники"))
        val placeId = database.placeDao().insert(
            PlaceEntity(
                categoryId = categoryId,
                authorId = authorId,
                title = "Проверяемое место",
                description = "Подробное описание места, ожидающего проверки модератором.",
                latitude = 58.01,
                longitude = 56.24,
                status = "PENDING"
            )
        )
        val queueId = database.moderationDao().insert(ModerationQueueEntity(entityType = "PLACE", entityId = placeId))

        val result = repository.approve(queueId, moderatorId, "ok")

        assertTrue(result.isSuccess)
        assertEquals("PUBLISHED", database.placeDao().getById(placeId)?.status)
        assertEquals(30, database.userDao().getById(authorId)?.points)
    }
}
