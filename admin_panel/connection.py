import asyncpg
from contextlib import asynccontextmanager

DATABASE_URL = "postgresql://user:password@localhost:5432/places_app_db"

@asynccontextmanager
async def get_db_connection():
    conn = await asyncpg.connect(DATABASE_URL)
    try:
        yield conn
    finally:
        await conn.close()

async def init_db():
    conn = await asyncpg.connect(DATABASE_URL)
    try:
        # Таблица users (добавляем login и password_hash для админов)
        await conn.execute("""
            ALTER TABLE users ADD COLUMN IF NOT EXISTS login VARCHAR(40),
            ADD COLUMN IF NOT EXISTS password_hash VARCHAR(100)
        """)
        
        # Таблица для истории изменений
        await conn.execute("""
            CREATE TABLE IF NOT EXISTS place_edit_history (
                uuid_history VARCHAR(36) PRIMARY KEY,
                uuid_place VARCHAR(36) REFERENCES places(uuid_place),
                changed_by VARCHAR(36) REFERENCES users(uuid_user),
                changed_at TIMESTAMP,
                old_value TEXT,
                new_value TEXT
            )
        """)
        
        # Таблица для связи user_exercises (добавляем date_assigned)
        await conn.execute("""
            ALTER TABLE user_exercises ADD COLUMN IF NOT EXISTS date_assigned TIMESTAMP
        """)
        
        # Таблица exercises (добавляем target_value, category, is_active)
        await conn.execute("""
            ALTER TABLE exercises ADD COLUMN IF NOT EXISTS target_value INTEGER DEFAULT 1,
            ADD COLUMN IF NOT EXISTS category VARCHAR(20) DEFAULT 'user',
            ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT true
        """)
        
        # Создание администратора по умолчанию
        admin_exists = await conn.fetchval("SELECT COUNT(*) FROM users WHERE login = 'admin'")
        if not admin_exists:
            await conn.execute("""
                INSERT INTO users (uuid_user, user_nm, role, login, password_hash, user_rating)
                VALUES ($1, 'Admin', 'admin', 'admin', $2, 0)
            """, str(uuid.uuid4()), hash_password("admin123"))
            
    finally:
        await conn.close()