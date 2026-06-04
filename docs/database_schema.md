# Схема базы данных

База переносит ER-диаграмму из диплома в Room/SQLite.

## Таблицы

- users
- categories
- places
- place_photos
- reviews
- ratings
- favorites
- reports
- achievements
- user_achievements
- tasks
- user_tasks
- routes
- route_places
- moderation_queue
- activity_log

## Основные связи

- users 1 → * places
- categories 1 → * places
- places 1 → * place_photos
- places 1 → * reviews
- places 1 → * ratings
- users * ↔ * achievements через user_achievements
- users * ↔ * tasks через user_tasks
- routes * ↔ * places через route_places
- moderation_queue хранит материалы на проверку
- activity_log фиксирует действия пользователя
