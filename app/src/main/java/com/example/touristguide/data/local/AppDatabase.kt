package com.example.touristguide.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.touristguide.core.constants.DatabaseConstants
import com.example.touristguide.data.local.dao.*
import com.example.touristguide.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        PlaceEntity::class,
        PlacePhotoEntity::class,
        ReviewEntity::class,
        RatingEntity::class,
        FavoriteEntity::class,
        ReportEntity::class,
        AchievementEntity::class,
        UserAchievementEntity::class,
        TaskEntity::class,
        UserTaskEntity::class,
        RouteEntity::class,
        RoutePlaceEntity::class,
        ModerationQueueEntity::class,
        ActivityLogEntity::class
    ],
    version = DatabaseConstants.DATABASE_VERSION,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun placeDao(): PlaceDao
    abstract fun placePhotoDao(): PlacePhotoDao
    abstract fun reviewDao(): ReviewDao
    abstract fun ratingDao(): RatingDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun reportDao(): ReportDao
    abstract fun achievementDao(): AchievementDao
    abstract fun taskDao(): TaskDao
    abstract fun routeDao(): RouteDao
    abstract fun moderationDao(): ModerationDao
    abstract fun activityLogDao(): ActivityLogDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DatabaseConstants.DATABASE_NAME
            ).build().also { INSTANCE = it }
        }
    }
}
