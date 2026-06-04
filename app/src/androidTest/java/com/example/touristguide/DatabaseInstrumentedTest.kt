package com.example.touristguide

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.touristguide.data.local.AppDatabase
import com.example.touristguide.data.local.DatabaseSeeder
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseInstrumentedTest {
    private lateinit var database: AppDatabase

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun seederCreatesCoreTablesData() = runTest {
        DatabaseSeeder(database).seedIfNeeded()

        assertTrue(database.userDao().count() >= 2)
        assertTrue(database.categoryDao().count() >= 4)
        assertTrue(database.placeDao().getByStatuses(listOf("PUBLISHED")).size >= 4)
    }
}
