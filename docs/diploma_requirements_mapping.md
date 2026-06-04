# Соответствие требованиям диплома

| Требование | Файлы проекта |
|---|---|
| Android + Kotlin | app/build.gradle, MainActivity.kt |
| MVVM | ui/*/*ViewModel.kt, data/repository |
| Room/SQLite | data/local/entity, data/local/dao, AppDatabase.kt |
| Карта OpenStreetMap | map/PlacesMapFragment.kt, map/RouteMapFragment.kt, osmdroid |
| Места | PlaceEntity, PlaceDao, PlaceRepository, places/ |
| Карточка места | place_detail/ |
| Отзывы | ReviewEntity, ReviewDao, review/ |
| Фото | PlacePhotoEntity, PlacePhotoDao, photo/ |
| Избранное | FavoriteEntity, FavoriteDao, favorites/ |
| Жалобы | ReportEntity, ReportDao, dialog_report.xml |
| Маршруты | RouteEntity, RoutePlaceEntity, routes/, RouteMapFragment.kt |
| Модерация | ModerationQueueEntity, ModerationDao, moderation/ |
| Достижения | AchievementEntity, UserAchievementEntity, achievements/ |
| Задания | TaskEntity, UserTaskEntity, tasks/ |
| Журнал действий | ActivityLogEntity, ActivityLogDao |
| Конвейер проверки контента | core/validation, core/moderation |
| Геймификация | core/gamification |
