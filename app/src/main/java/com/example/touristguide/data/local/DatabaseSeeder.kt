package com.example.touristguide.data.local

import androidx.room.withTransaction
import com.example.touristguide.data.local.entity.*

class DatabaseSeeder(private val database: AppDatabase) {
    suspend fun seedIfNeeded() {
        if (database.placeDao().getByStatuses(listOf("PUBLISHED", "PENDING", "POSSIBLE_DUPLICATE")).isNotEmpty()) return

        database.withTransaction {
            val adminId = database.userDao().findByEmail("moderator@local")?.id ?: database.userDao().insert(
                    UserEntity(username = "Модератор", email = "moderator@local", password = "1234", role = "MODERATOR", points = 250, level = 3)
                )
            val userId = database.userDao().findByEmail("guest@local")?.id ?: database.userDao().insert(
                    UserEntity(username = "Иван Иванов", email = "guest@local", password = "1234")
                )

            val monuments = database.categoryDao().findByName("Памятники и скульптуры")?.id
                ?: database.categoryDao().insert(CategoryEntity(name = "Памятники и скульптуры", description = "Исторические памятники города"))
            if (database.categoryDao().findByName("Исторические здания") == null) {
                database.categoryDao().insert(CategoryEntity(name = "Исторические здания", description = "Архитектура и городская память"))
            }
            if (database.categoryDao().findByName("Музеи") == null) {
                database.categoryDao().insert(CategoryEntity(name = "Музеи", description = "Музейные объекты"))
            }
            val food = database.categoryDao().findByName("Где поесть")?.id
                ?: database.categoryDao().insert(CategoryEntity(name = "Где поесть", description = "Места рядом с туристическими маршрутами"))

            val seedTime = System.currentTimeMillis()
            val place1 = database.placeDao().insert(PlaceEntity(categoryId = monuments, authorId = adminId, title = "«Легенда о Пермском медведе»", description = "Скульптура «Легенда о пермском медведе» — один из узнаваемых символов Перми и популярная точка туристических маршрутов.", latitude = 58.01083, longitude = 56.23778, address = "г. Пермь", status = "PUBLISHED", averageRating = 4.9, createdAt = seedTime + 4000, updatedAt = seedTime + 4000))
            val place2 = database.placeDao().insert(PlaceEntity(categoryId = monuments, authorId = adminId, title = "«Пермяк соленые уши»", description = "Городская скульптура, связанная с историей солеварения и ставшая одним из самых фотографируемых объектов центра Перми.", latitude = 58.01024, longitude = 56.22942, address = "г. Пермь", status = "PUBLISHED", averageRating = 4.8, createdAt = seedTime + 3000, updatedAt = seedTime + 3000))
            val place3 = database.placeDao().insert(PlaceEntity(categoryId = monuments, authorId = adminId, title = "Пермская городская эспланада", description = "Открытое городское пространство для прогулок, событий и знакомства с центральной частью Перми.", latitude = 58.0105, longitude = 56.2502, address = "Пермь, центр", status = "PUBLISHED", averageRating = 4.7, createdAt = seedTime + 2000, updatedAt = seedTime + 2000))
            val place4 = database.placeDao().insert(PlaceEntity(categoryId = food, authorId = adminId, title = "Кафе у набережной", description = "Точка отдыха рядом с прогулочным маршрутом, которую удобно добавить в туристический день.", latitude = 58.0187, longitude = 56.2531, address = "Пермь, набережная", status = "PUBLISHED", averageRating = 4.4, createdAt = seedTime + 1000, updatedAt = seedTime + 1000))
            val pendingPlace = database.placeDao().insert(PlaceEntity(categoryId = monuments, authorId = userId, title = "Новая точка маршрута", description = "Пользователь предложил место для прогулки; запись создана как пример материала, ожидающего модерации.", latitude = 58.012, longitude = 56.252, address = "Пермь", status = "PENDING"))

            database.placePhotoDao().insert(PlacePhotoEntity(placeId = place1, userId = adminId, filePath = "asset://perm_esplanade.jpg", status = "PUBLISHED"))
            database.placePhotoDao().insert(PlacePhotoEntity(placeId = place2, userId = adminId, filePath = "asset://gribushin_house.jpg", status = "PUBLISHED"))
            database.reviewDao().insert(ReviewEntity(placeId = place1, userId = userId, text = "Удобная точка для начала прогулки по центру города."))
            database.ratingDao().insert(RatingEntity(placeId = place1, userId = userId, value = 5))
            database.favoriteDao().insert(FavoriteEntity(userId = userId, placeId = place1))

            val greenRouteId = database.routeDao().insert(RouteEntity(authorId = adminId, title = "Зеленая линия", description = "Пешеходный маршрут по ключевым историческим объектам Перми.", durationMinutes = 90, distanceKm = 4.2))
            database.routeDao().insertRoutePlace(RoutePlaceEntity(routeId = greenRouteId, placeId = place1, position = 1))
            database.routeDao().insertRoutePlace(RoutePlaceEntity(routeId = greenRouteId, placeId = place2, position = 2))
            database.routeDao().insertRoutePlace(RoutePlaceEntity(routeId = greenRouteId, placeId = place3, position = 3))

            val redRouteId = database.routeDao().insert(RouteEntity(authorId = adminId, title = "Красная линия", description = "Маршрут для знакомства с культурными объектами и городскими историями.", durationMinutes = 70, distanceKm = 3.5))
            database.routeDao().insertRoutePlace(RoutePlaceEntity(routeId = redRouteId, placeId = place3, position = 1))
            database.routeDao().insertRoutePlace(RoutePlaceEntity(routeId = redRouteId, placeId = place4, position = 2))

            val imperialRouteId = database.routeDao().insert(RouteEntity(authorId = adminId, title = "Императорская линия", description = "Маршрут по городским местам, связанным с историей, архитектурой и культурной памятью Перми.", durationMinutes = 80, distanceKm = 3.8))
            database.routeDao().insertRoutePlace(RoutePlaceEntity(routeId = imperialRouteId, placeId = place1, position = 1))
            database.routeDao().insertRoutePlace(RoutePlaceEntity(routeId = imperialRouteId, placeId = place2, position = 2))
            database.routeDao().insertRoutePlace(RoutePlaceEntity(routeId = imperialRouteId, placeId = place3, position = 3))

            val achievementId = database.achievementDao().insert(AchievementEntity(title = "Первый отзыв", description = "Оставить первый отзыв о месте", conditionType = "REVIEWS_COUNT", conditionValue = 1, pointsReward = 10))
            database.achievementDao().insert(AchievementEntity(title = "Автор места", description = "Добавить первое место и пройти модерацию", conditionType = "PLACES_COUNT", conditionValue = 1, pointsReward = 30))
            database.taskDao().insert(TaskEntity(title = "Исследователь недели", description = "Добавить одно место или отзыв за неделю", taskType = "WEEKLY_ACTIVITY", targetValue = 1, pointsReward = 25, isWeekly = true))
            database.achievementDao().insertUserAchievement(UserAchievementEntity(userId = userId, achievementId = achievementId))

            database.moderationDao().insert(ModerationQueueEntity(entityType = "PLACE", entityId = pendingPlace))
            database.activityLogDao().insert(ActivityLogEntity(userId = userId, actionType = "ADD_PLACE", entityType = "PLACE", entityId = pendingPlace))
            database.activityLogDao().insert(ActivityLogEntity(userId = userId, actionType = "ADD_REVIEW", entityType = "REVIEW", entityId = 1))
        }
    }
}
