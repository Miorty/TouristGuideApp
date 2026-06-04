package com.example.touristguide.data

import android.content.Context
import com.example.touristguide.core.session.SessionManager
import com.example.touristguide.data.local.AppDatabase
import com.example.touristguide.data.local.DatabaseSeeder
import com.example.touristguide.data.repository.AchievementRepository
import com.example.touristguide.data.repository.ActivityLogRepository
import com.example.touristguide.data.repository.AuthRepository
import com.example.touristguide.data.repository.CategoryRepository
import com.example.touristguide.data.repository.FavoriteRepository
import com.example.touristguide.data.repository.ModerationRepository
import com.example.touristguide.data.repository.PhotoRepository
import com.example.touristguide.data.repository.PlaceRepository
import com.example.touristguide.data.repository.RatingRepository
import com.example.touristguide.data.repository.ReportRepository
import com.example.touristguide.data.repository.ReviewRepository
import com.example.touristguide.data.repository.RouteRepository
import com.example.touristguide.data.repository.TaskRepository
import com.example.touristguide.data.repository.UserRepository

class AppContainer(context: Context) {
    val database: AppDatabase = AppDatabase.getInstance(context)
    val sessionManager = SessionManager()

    val authRepository = AuthRepository(database.userDao(), database.activityLogDao())
    val userRepository = UserRepository(database.userDao())
    val categoryRepository = CategoryRepository(database.categoryDao())
    val activityLogRepository = ActivityLogRepository(database.activityLogDao())
    val placeRepository = PlaceRepository(database, database.placeDao(), database.moderationDao(), database.activityLogDao())
    val reviewRepository = ReviewRepository(database, database.reviewDao(), database.moderationDao(), database.activityLogDao())
    val photoRepository = PhotoRepository(database, database.placePhotoDao(), database.moderationDao(), database.activityLogDao())
    val ratingRepository = RatingRepository(database.ratingDao(), database.placeDao())
    val favoriteRepository = FavoriteRepository(database, database.favoriteDao(), database.placeDao(), database.activityLogDao())
    val reportRepository = ReportRepository(database, database.reportDao(), database.moderationDao(), database.activityLogDao())
    val achievementRepository = AchievementRepository(database, database.achievementDao(), database.userDao(), database.activityLogDao())
    val taskRepository = TaskRepository(database, database.taskDao(), database.userDao(), database.activityLogDao())
    val routeRepository = RouteRepository(database, database.routeDao(), database.placeDao())
    val moderationRepository = ModerationRepository(
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

    suspend fun seedIfNeeded() {
        DatabaseSeeder(database).seedIfNeeded()
    }
}
