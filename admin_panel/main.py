from fastapi import FastAPI, Request, Form, Depends, HTTPException, UploadFile, File
from fastapi.responses import HTMLResponse, RedirectResponse
from fastapi.templating import Jinja2Templates
from fastapi.staticfiles import StaticFiles
from fastapi.security import HTTPBasicCredentials
from typing import Optional, List
import asyncpg
import uuid
import hashlib
import os
from datetime import datetime
import json

app = FastAPI(title="Admin Panel")

# Подключение статики и шаблонов
app.mount("/static", StaticFiles(directory="static"), name="static")
templates = Jinja2Templates(directory="templates")

# Конфигурация БД
DATABASE_URL = "postgresql://user:password@localhost:5432/places_app_db"

# Хеширование пароля
def hash_password(password: str) -> str:
    return hashlib.pbkdf2_hmac('sha256', password.encode(), b'salt', 100000).hex()

def verify_password(password: str, hashed: str) -> bool:
    return hash_password(password) == hashed

# Получение сессии администратора
async def get_admin_session(request: Request):
    session_id = request.cookies.get("session_id")
    if not session_id:
        return None
    # Проверка сессии в БД (упрощенно)
    return {"user_id": 1, "role": "admin"}

# Подключение к БД
async def get_db():
    conn = await asyncpg.connect(DATABASE_URL)
    try:
        yield conn
    finally:
        await conn.close()

# ========== Страница входа ==========
@app.get("/admin/login", response_class=HTMLResponse)
async def login_page(request: Request):
    return templates.TemplateResponse("login.html", {"request": request})

@app.post("/admin/login")
async def login(request: Request, login: str = Form(...), password: str = Form(...)):
    conn = await asyncpg.connect(DATABASE_URL)
    try:
        row = await conn.fetchrow(
            "SELECT uuid_user, login, password_hash, role FROM users WHERE login = $1 AND role IN ('admin', 'moderator')",
            login
        )
        if row and verify_password(password, row['password_hash']):
            response = RedirectResponse(url="/admin/dashboard", status_code=303)
            response.set_cookie(key="session_id", value=str(row['uuid_user']))
            return response
        return templates.TemplateResponse("login.html", {"request": request, "error": "Неверный логин или пароль"})
    finally:
        await conn.close()

@app.get("/admin/logout")
async def logout():
    response = RedirectResponse(url="/admin/login")
    response.delete_cookie("session_id")
    return response

# ========== Главная страница ==========
@app.get("/admin/dashboard", response_class=HTMLResponse)
async def dashboard(request: Request):
    return templates.TemplateResponse("dashboard.html", {"request": request})

# ========== 1. Управление местами ==========
@app.get("/admin/places", response_class=HTMLResponse)
async def places_manage(request: Request):
    conn = await asyncpg.connect(DATABASE_URL)
    try:
        places = await conn.fetch("""
            SELECT p.uuid_place, p.place_nm, p.brief_description, p.status_place, 
                   p.created_at, u.user_nm as author
            FROM places p
            LEFT JOIN users u ON p.created_uuid_user = u.uuid_user
            ORDER BY p.created_at DESC
        """)
        categories = await conn.fetch("SELECT uuid_category, category_nm FROM categories")
        return templates.TemplateResponse("places_manage.html", {
            "request": request, 
            "places": places,
            "categories": categories
        })
    finally:
        await conn.close()

@app.post("/admin/api/place/add")
async def add_place(
    place_nm: str = Form(...),
    category_uuid: str = Form(...),
    brief_description: str = Form(...),
    historical_background: str = Form(...),
    latitude: float = Form(...),
    longitude: float = Form(...),
    locality: str = Form(...),
    address: str = Form(""),
    request: Request = None
):
    conn = await asyncpg.connect(DATABASE_URL)
    try:
        place_uuid = str(uuid.uuid4())
        await conn.execute("""
            INSERT INTO places (uuid_place, place_nm, brief_description, historical_background,
                                latitude, longitude, locality, address, status_place, 
                                created_at, created_uuid_user, rating, likes_place)
            VALUES ($1, $2, $3, $4, $5, $6, $7, $8, 'approved', NOW(), $9, 0, 0)
        """, place_uuid, place_nm, brief_description, historical_background,
            latitude, longitude, locality, address, "00000000-0000-0000-0000-000000000001")
        
        await conn.execute(
            "INSERT INTO place_category (uuid_place, uuid_category) VALUES ($1, $2)",
            place_uuid, category_uuid
        )
        return {"success": True, "place_uuid": place_uuid}
    finally:
        await conn.close()

@app.post("/admin/api/place/{place_uuid}/update")
async def update_place(place_uuid: str, place_nm: str = Form(...), brief_description: str = Form(...)):
    conn = await asyncpg.connect(DATABASE_URL)
    try:
        # Сохраняем старую версию для истории
        old = await conn.fetchrow("SELECT * FROM places WHERE uuid_place = $1", place_uuid)
        
        await conn.execute("""
            UPDATE places SET place_nm = $1, brief_description = $2 
            WHERE uuid_place = $3
        """, place_nm, brief_description, place_uuid)
        
        # Логируем изменение
        await conn.execute("""
            INSERT INTO place_edit_history (uuid_place, changed_by, changed_at, old_value, new_value)
            VALUES ($1, $2, NOW(), $3, $4)
        """, place_uuid, "admin", 
            json.dumps({"name": old['place_nm']}), 
            json.dumps({"name": place_nm}))
        
        return {"success": True}
    finally:
        await conn.close()

@app.post("/admin/api/place/{place_uuid}/delete")
async def delete_place(place_uuid: str):
    conn = await asyncpg.connect(DATABASE_URL)
    try:
        # Проверка зависимостей
        routes = await conn.fetchval(
            "SELECT COUNT(*) FROM place_in_ready_route WHERE uuid_place = $1", place_uuid
        )
        reviews = await conn.fetchval(
            "SELECT COUNT(*) FROM reviews WHERE uuid_place = $1", place_uuid
        )
        if routes > 0 or reviews > 0:
            return {"success": False, "error": "Место связано с маршрутами или отзывами"}
        
        await conn.execute("DELETE FROM places WHERE uuid_place = $1", place_uuid)
        return {"success": True}
    finally:
        await conn.close()

# ========== 2. Управление маршрутами ==========
@app.get("/admin/routes", response_class=HTMLResponse)
async def routes_manage(request: Request):
    conn = await asyncpg.connect(DATABASE_URL)
    try:
        routes = await conn.fetch("""
            SELECT rr.uuid_ready_route, rr.ready_route_nm, COUNT(pirr.uuid_place) as places_count
            FROM ready_routes rr
            LEFT JOIN place_in_ready_route pirr ON rr.uuid_ready_route = pirr.uuid_ready_route
            GROUP BY rr.uuid_ready_route
        """)
        places = await conn.fetch("SELECT uuid_place, place_nm FROM places WHERE status_place = 'approved'")
        return templates.TemplateResponse("routes_manage.html", {
            "request": request, 
            "routes": routes,
            "places": places
        })
    finally:
        await conn.close()

@app.post("/admin/api/route/add")
async def add_route(route_nm: str = Form(...), places_ids: str = Form(...)):
    conn = await asyncpg.connect(DATABASE_URL)
    try:
        route_uuid = str(uuid.uuid4())
        await conn.execute(
            "INSERT INTO ready_routes (uuid_ready_route, ready_route_nm, created_uuid_user, crated_at) VALUES ($1, $2, $3, NOW())",
            route_uuid, route_nm, "00000000-0000-0000-0000-000000000001"
        )
        places_list = places_ids.split(',')
        for idx, place_id in enumerate(places_list):
            await conn.execute(
                "INSERT INTO place_in_ready_route (uuid_place, uuid_ready_route, place_num) VALUES ($1, $2, $3)",
                place_id, route_uuid, idx
            )
        return {"success": True}
    finally:
        await conn.close()

# ========== 3. Управление достижениями ==========
@app.get("/admin/achievements", response_class=HTMLResponse)
async def achievements_manage(request: Request):
    conn = await asyncpg.connect(DATABASE_URL)
    try:
        achievements = await conn.fetch("SELECT * FROM achievements ORDER BY created_at DESC")
        return templates.TemplateResponse("achievements_manage.html", {
            "request": request, 
            "achievements": achievements
        })
    finally:
        await conn.close()

@app.post("/admin/api/achievement/add")
async def add_achievement(
    achievement_nm: str = Form(...),
    description: str = Form(...),
    reward_points: int = Form(...),
    icon: UploadFile = File(...)
):
    conn = await asyncpg.connect(DATABASE_URL)
    try:
        icon_filename = f"{uuid.uuid4()}.png"
        icon_path = f"static/achievements/{icon_filename}"
        with open(icon_path, "wb") as f:
            f.write(await icon.read())
        
        await conn.execute("""
            INSERT INTO achievements (uuid_achievement, achievement_nm, description, icon, reward_points, created_uuid_admin)
            VALUES ($1, $2, $3, $4, $5, $6)
        """, str(uuid.uuid4()), achievement_nm, description, icon_path, reward_points, "admin")
        return {"success": True}
    finally:
        await conn.close()

# ========== 4. Управление заданиями ==========
@app.get("/admin/tasks", response_class=HTMLResponse)
async def tasks_manage(request: Request):
    conn = await asyncpg.connect(DATABASE_URL)
    try:
        tasks = await conn.fetch("SELECT * FROM exercises ORDER BY created_at DESC")
        return templates.TemplateResponse("tasks_manage.html", {"request": request, "tasks": tasks})
    finally:
        await conn.close()

@app.post("/admin/api/task/add")
async def add_task(
    exercise_nm: str = Form(...),
    description: str = Form(...),
    target_value: int = Form(...),
    category: str = Form(...),
    is_active: bool = Form(False)
):
    conn = await asyncpg.connect(DATABASE_URL)
    try:
        await conn.execute("""
            INSERT INTO exercises (uuid_exercise, exercise_nm, description, target_value, category, is_active, created_uuid_admin)
            VALUES ($1, $2, $3, $4, $5, $6, $7)
        """, str(uuid.uuid4()), exercise_nm, description, target_value, category, is_active, "admin")
        return {"success": True}
    finally:
        await conn.close()

@app.post("/admin/api/tasks/generate")
async def generate_weekly_tasks():
    conn = await asyncpg.connect(DATABASE_URL)
    try:
        # Выбираем 3-5 активных заданий
        tasks = await conn.fetch(
            "SELECT uuid_exercise FROM exercises WHERE is_active = true ORDER BY RANDOM() LIMIT 5"
        )
        users = await conn.fetch("SELECT uuid_user FROM users WHERE role = 'user'")
        
        for user in users:
            for task in tasks:
                await conn.execute("""
                    INSERT INTO user_exercises (uuid_user, uuid_exercise, completed, date_assigned)
                    VALUES ($1, $2, false, NOW())
                    ON CONFLICT DO NOTHING
                """, user['uuid_user'], task['uuid_exercise'])
        return {"success": True, "count": len(tasks) * len(users)}
    finally:
        await conn.close()

# ========== 5. Модерация мест ==========
@app.get("/admin/moderation/places", response_class=HTMLResponse)
async def moderation_places(request: Request):
    conn = await asyncpg.connect(DATABASE_URL)
    try:
        places = await conn.fetch("""
            SELECT p.*, u.user_nm as author_name, u.user_rating as author_rating
            FROM places p
            LEFT JOIN users u ON p.created_uuid_user = u.uuid_user
            WHERE p.status_place = 'pending'
            ORDER BY p.created_at ASC
        """)
        return templates.TemplateResponse("moderation_places.html", {"request": request, "places": places})
    finally:
        await conn.close()

@app.post("/admin/api/place/{place_uuid}/approve")
async def approve_place(place_uuid: str):
    conn = await asyncpg.connect(DATABASE_URL)
    try:
        async with conn.transaction():
            # Получаем автора
            place = await conn.fetchrow("SELECT created_uuid_user FROM places WHERE uuid_place = $1", place_uuid)
            if not place:
                return {"success": False, "error": "Место не найдено"}
            
            # Обновляем статус
            await conn.execute(
                "UPDATE places SET status_place = 'approved' WHERE uuid_place = $1",
                place_uuid
            )
            
            # Начисляем XP автору (+50)
            await conn.execute(
                "UPDATE users SET user_rating = user_rating + 50 WHERE uuid_user = $1",
                place['created_uuid_user']
            )
            
            # Создаем запись в журнале модерации
            await conn.execute("""
                INSERT INTO moderation (uuid_place, uuid_user_creator, uuid_user_moderator, comment, status_moderation, checked_at)
                VALUES ($1, $2, $3, 'Одобрено', 'approved', NOW())
            """, place_uuid, place['created_uuid_user'], "00000000-0000-0000-0000-000000000001")
            
        return {"success": True}
    finally:
        await conn.close()

@app.post("/admin/api/place/{place_uuid}/reject")
async def reject_place(place_uuid: str, reason: str = Form(...)):
    conn = await asyncpg.connect(DATABASE_URL)
    try:
        async with conn.transaction():
            place = await conn.fetchrow("SELECT created_uuid_user FROM places WHERE uuid_place = $1", place_uuid)
            
            await conn.execute(
                "UPDATE places SET status_place = 'rejected' WHERE uuid_place = $1",
                place_uuid
            )
            
            await conn.execute("""
                INSERT INTO moderation (uuid_place, uuid_user_creator, uuid_user_moderator, comment, status_moderation, checked_at)
                VALUES ($1, $2, $3, $4, 'rejected', NOW())
            """, place_uuid, place['created_uuid_user'], "moderator_id", reason)
            
        return {"success": True}
    finally:
        await conn.close()

# ========== 6. Модерация жалоб ==========
@app.get("/admin/moderation/complaints", response_class=HTMLResponse)
async def moderation_complaints(request: Request):
    conn = await asyncpg.connect(DATABASE_URL)
    try:
        complaints = await conn.fetch("""
            SELECT c.*, u.user_nm as author_name
            FROM complaints c
            LEFT JOIN users u ON c.uuid_creator = u.uuid_user
            WHERE c.status_complain = 'new'
            ORDER BY c.create_at ASC
        """)
        return templates.TemplateResponse("moderation_complaints.html", {"request": request, "complaints": complaints})
    finally:
        await conn.close()

@app.post("/admin/api/complaint/{complaint_uuid}/accept")
async def accept_complaint(complaint_uuid: str):
    conn = await asyncpg.connect(DATABASE_URL)
    try:
        async with conn.transaction():
            complaint = await conn.fetchrow("SELECT * FROM complaints WHERE uuid_complaint = $1", complaint_uuid)
            
            if complaint['chapter'] == 'place':
                await conn.execute(
                    "UPDATE places SET status_place = 'suspended' WHERE uuid_place = $1",
                    complaint['uuid_place']
                )
            else:
                await conn.execute(
                    "UPDATE reviews SET is_hidden = true WHERE uuid_reviews = $1",
                    complaint['uuid_reviews']
                )
            
            # Начисляем XP автору жалобы
            await conn.execute(
                "UPDATE users SET user_rating = user_rating + 30 WHERE uuid_user = $1",
                complaint['uuid_creator']
            )
            
            await conn.execute(
                "UPDATE complaints SET status_complain = 'accepted', checked_at = NOW() WHERE uuid_complaint = $1",
                complaint_uuid
            )
            
        return {"success": True}
    finally:
        await conn.close()

@app.post("/admin/api/complaint/{complaint_uuid}/reject")
async def reject_complaint(complaint_uuid: str):
    conn = await asyncpg.connect(DATABASE_URL)
    try:
        await conn.execute(
            "UPDATE complaints SET status_complain = 'rejected', checked_at = NOW() WHERE uuid_complaint = $1",
            complaint_uuid
        )
        return {"success": True}
    finally:
        await conn.close()

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)