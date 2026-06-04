
CREATE DATABASE places_app_db
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'ru_RU.UTF-8'
    LC_CTYPE = 'ru_RU.UTF-8'
    TEMPLATE = template0;

CREATE TABLE users (
    uuid_user varchar(36) PRIMARY KEY,
    user_nm varchar(50) NOT NULL,
    role varchar(20) NOT NULL,
    auth_provider varchar(20) NOT NULL,
    sub varchar(100),
    login varchar(40),
    password_hash varchar(100),
    user_rating integer DEFAULT 0,
    moderator_rating integer DEFAULT 0,

    CONSTRAINT chk_users_role
        CHECK (role IN ('user', 'moderator', 'admin')),

    CONSTRAINT chk_users_auth_provider
        CHECK (auth_provider IN ('vk', 'local'))
);

CREATE TABLE categories (
    uuid_category varchar(36) NOT NULL,
    category_nm varchar(60) NOT NULL,

    CONSTRAINT pk_categories
        PRIMARY KEY (uuid_category),

    CONSTRAINT uq_categories_name
        UNIQUE (category_nm)
);

CREATE TABLE places (
    uuid_place varchar(36) PRIMARY KEY,

    place_nm varchar(300) NOT NULL,
    brief_description text NOT NULL,

    -- В исходной модели поле называется historical_bacground.
    -- Лучше в будущем переименовать в historical_background.
    historical_bacground text NOT NULL,

    audio text,
    address varchar(300),
    locality varchar(60) NOT NULL,

    latitude decimal(9,6) NOT NULL,
    longitude decimal(9,6) NOT NULL,

    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_uuid_user varchar(36) NOT NULL,

    likes_place integer NOT NULL DEFAULT 0,
    rating integer NOT NULL DEFAULT 0,

    status_place varchar(40) NOT NULL DEFAULT 'на модерации',

    CONSTRAINT fk_places_created_user
        FOREIGN KEY (created_uuid_user)
        REFERENCES users(uuid_user)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT chk_places_latitude
        CHECK (latitude >= -90 AND latitude <= 90),

    CONSTRAINT chk_places_longitude
        CHECK (longitude >= -180 AND longitude <= 180),

    CONSTRAINT chk_places_likes
        CHECK (likes_place >= 0),

    CONSTRAINT chk_places_rating
        CHECK (rating >= 0),

    CONSTRAINT chk_places_status
        CHECK (
            status_place IN (
                'на модерации',
                'одобрено',
                'отклонено',
                'не требует проверки'
            )
        )
);

CREATE TABLE place_category (
    uuid_category varchar(36) NOT NULL,
    uuid_place varchar(36) NOT NULL,

    CONSTRAINT pk_place_category
        PRIMARY KEY (uuid_category, uuid_place),

    CONSTRAINT fk_place_category_category
        FOREIGN KEY (uuid_category)
        REFERENCES categories(uuid_category)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT fk_place_category_place
        FOREIGN KEY (uuid_place)
        REFERENCES places(uuid_place)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE photo (
    uuid_photo varchar(36) PRIMARY KEY,
    num_photo integer NOT NULL,
    url_photo text NOT NULL,
    uuid_place varchar(36) NOT NULL,

    CONSTRAINT fk_photo_place
        FOREIGN KEY (uuid_place)
        REFERENCES places(uuid_place)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT chk_photo_num
        CHECK (num_photo > 0)
);


CREATE TABLE place_likes (
    uuid_place varchar(36) NOT NULL,
    uuid_user varchar(36) NOT NULL,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_place_likes
        PRIMARY KEY (uuid_place, uuid_user),

    CONSTRAINT fk_place_likes_place
        FOREIGN KEY (uuid_place)
        REFERENCES places(uuid_place)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT fk_place_likes_user
        FOREIGN KEY (uuid_user)
        REFERENCES users(uuid_user)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE ready_routes (
    uuid_ready_route varchar(36) PRIMARY KEY,
    ready_route_nm varchar(200) NOT NULL,
    created_uuid_user varchar(36) NOT NULL,

    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ready_routes_user
        FOREIGN KEY (created_uuid_user)
        REFERENCES users(uuid_user)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);


CREATE TABLE place_in_ready_router (
    uuid_place varchar(36) NOT NULL,
    uuid_ready_route varchar(36) NOT NULL,
    place_num integer NOT NULL,

    CONSTRAINT pk_place_in_ready_router
        PRIMARY KEY (uuid_place, uuid_ready_route),

    CONSTRAINT fk_place_in_ready_router_place
        FOREIGN KEY (uuid_place)
        REFERENCES places(uuid_place)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT fk_place_in_ready_router_route
        FOREIGN KEY (uuid_ready_route)
        REFERENCES ready_routes(uuid_ready_route)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT chk_place_in_ready_router_num
        CHECK (place_num > 0)
);

CREATE TABLE favorite_places (
    uuid_user varchar(36) NOT NULL,
    uuid_place varchar(36) NOT NULL,

    CONSTRAINT pk_favorite_places
        PRIMARY KEY (uuid_user, uuid_place),

    CONSTRAINT fk_favorite_places_user
        FOREIGN KEY (uuid_user)
        REFERENCES users(uuid_user)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT fk_favorite_places_place
        FOREIGN KEY (uuid_place)
        REFERENCES places(uuid_place)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE user_routes (
    uuid_route varchar(36) PRIMARY KEY,
    route_nm varchar(50) NOT NULL,
    uuid_user varchar(36) NOT NULL,

    CONSTRAINT fk_user_routes_user
        FOREIGN KEY (uuid_user)
        REFERENCES users(uuid_user)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE place_in_user_route (
    uuid_route varchar(36) NOT NULL,
    uuid_place varchar(36) NOT NULL,
    place_num integer NOT NULL,

    CONSTRAINT pk_place_in_user_route
        PRIMARY KEY (uuid_route, uuid_place),

    CONSTRAINT fk_place_in_user_route_route
        FOREIGN KEY (uuid_route)
        REFERENCES user_routes(uuid_route)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT fk_place_in_user_route_place
        FOREIGN KEY (uuid_place)
        REFERENCES places(uuid_place)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT chk_place_in_user_route_num
        CHECK (place_num > 0)
);

CREATE TABLE reviews (
    uuid_reviews varchar(36) PRIMARY KEY,

    uuid_user varchar(36) NOT NULL,
    uuid_place varchar(36) NOT NULL,

    photo text,
    text text NOT NULL,
    grade integer,

    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    likes_reviews integer NOT NULL DEFAULT 0,

    is_interesting_fact boolean NOT NULL DEFAULT false,

    -- Рекомендуемое поле, которого нет в исходной модели,
    -- но оно удобно для связки с микросервисом фильтрации.
    status_review varchar(40) NOT NULL DEFAULT 'published',

    CONSTRAINT fk_reviews_user
        FOREIGN KEY (uuid_user)
        REFERENCES users(uuid_user)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT fk_reviews_place
        FOREIGN KEY (uuid_place)
        REFERENCES places(uuid_place)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT chk_reviews_grade
        CHECK (grade IS NULL OR grade BETWEEN 1 AND 5),

    CONSTRAINT chk_reviews_likes
        CHECK (likes_reviews >= 0),

    CONSTRAINT chk_reviews_status
        CHECK (
            status_review IN (
                'published',
                'на модерации',
                'отклонено'
            )
        )
);

CREATE TABLE review_likes (
    uuid_review varchar(36) NOT NULL,
    uuid_user varchar(36) NOT NULL,
    create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_review_likes
        PRIMARY KEY (uuid_review, uuid_user),

    CONSTRAINT fk_review_likes_review
        FOREIGN KEY (uuid_review)
        REFERENCES reviews(uuid_reviews)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT fk_review_likes_user
        FOREIGN KEY (uuid_user)
        REFERENCES users(uuid_user)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE complaint (
    uuid_complaint varchar(36) PRIMARY KEY,

    chapter varchar(10) NOT NULL,

    uuid_place varchar(36),
    uuid_reviews varchar(36),

    uuid_creator varchar(36) NOT NULL,

    complaint_reason text NOT NULL,
    status_complain varchar(20) NOT NULL DEFAULT 'создана',

    create_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,

    cheked_uuid_admin varchar(36),
    comment_admin text,
    checked_at timestamp,

    CONSTRAINT fk_complaint_place
        FOREIGN KEY (uuid_place)
        REFERENCES places(uuid_place)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT fk_complaint_review
        FOREIGN KEY (uuid_reviews)
        REFERENCES reviews(uuid_reviews)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT fk_complaint_creator
        FOREIGN KEY (uuid_creator)
        REFERENCES users(uuid_user)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT fk_complaint_admin
        FOREIGN KEY (cheked_uuid_admin)
        REFERENCES users(uuid_user)
        ON UPDATE CASCADE
        ON DELETE SET NULL,

    CONSTRAINT chk_complaint_chapter
        CHECK (chapter IN ('place', 'review')),

    CONSTRAINT chk_complaint_target
        CHECK (
            (chapter = 'place' AND uuid_place IS NOT NULL AND uuid_reviews IS NULL)
            OR
            (chapter = 'review' AND uuid_reviews IS NOT NULL AND uuid_place IS NULL)
        ),

    CONSTRAINT chk_complaint_status
        CHECK (
            status_complain IN (
                'создана',
                'на рассмотрении',
                'отклонена',
                'принята'
            )
        )
);

CREATE TABLE moderation (
    uuid_moderation varchar(36) PRIMARY KEY,

    uuid_place varchar(36) NOT NULL,
    uuid_user_creator varchar(36) NOT NULL,

    -- На этапе создания записи модератор может быть ещё не назначен,
    -- поэтому поле сделано nullable.
    uuid_user_moderator varchar(36),

    -- В исходной модели comment varchar(36), но для комментария лучше text.
    comment text,

    status_moderation varchar(20) NOT NULL DEFAULT 'new',
    checked_at timestamp,

    CONSTRAINT fk_moderation_place
        FOREIGN KEY (uuid_place)
        REFERENCES places(uuid_place)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT fk_moderation_creator
        FOREIGN KEY (uuid_user_creator)
        REFERENCES users(uuid_user)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT fk_moderation_moderator
        FOREIGN KEY (uuid_user_moderator)
        REFERENCES users(uuid_user)
        ON UPDATE CASCADE
        ON DELETE SET NULL,

    CONSTRAINT chk_moderation_status
        CHECK (
            status_moderation IN (
                'approved',
                'rejected',
                'new'
            )
        )
);


CREATE TABLE exercises (
    uuid_exercise varchar(36) PRIMARY KEY,

    exercise_nm varchar(100) NOT NULL,
    description varchar(300),

    created_uuid_admin varchar(36) NOT NULL,

    CONSTRAINT fk_exercises_admin
        FOREIGN KEY (created_uuid_admin)
        REFERENCES users(uuid_user)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);


CREATE TABLE user_exercises (
    uuid_user varchar(36) NOT NULL,
    uuid_exercise varchar(36) NOT NULL,
    completed boolean NOT NULL DEFAULT false,

    CONSTRAINT pk_user_exercises
        PRIMARY KEY (uuid_user, uuid_exercise),

    CONSTRAINT fk_user_exercises_user
        FOREIGN KEY (uuid_user)
        REFERENCES users(uuid_user)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT fk_user_exercises_exercise
        FOREIGN KEY (uuid_exercise)
        REFERENCES exercises(uuid_exercise)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE achievements (
    uuid_achievement varchar(36) PRIMARY KEY,

    achievement_nm varchar(50) NOT NULL,
    description text NOT NULL,
    icon text NOT NULL,
    reward_points integer DEFAULT 0,

    created_uuid_admin varchar(36) NOT NULL,

    CONSTRAINT fk_achievements_admin
        FOREIGN KEY (created_uuid_admin)
        REFERENCES users(uuid_user)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT chk_achievements_reward
        CHECK (reward_points IS NULL OR reward_points >= 0)
);


CREATE TABLE awarded_achievements (
    uuid_user varchar(36) NOT NULL,
    uuid_achievement varchar(36) NOT NULL,

    CONSTRAINT pk_awarded_achievements
        PRIMARY KEY (uuid_user, uuid_achievement),

    CONSTRAINT fk_awarded_achievements_user
        FOREIGN KEY (uuid_user)
        REFERENCES users(uuid_user)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT fk_awarded_achievements_achievement
        FOREIGN KEY (uuid_achievement)
        REFERENCES achievements(uuid_achievement)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);


CREATE INDEX idx_places_locality
    ON places(locality);

CREATE INDEX idx_places_coordinates
    ON places(latitude, longitude);

CREATE INDEX idx_places_status
    ON places(status_place);

CREATE INDEX idx_places_created_user
    ON places(created_uuid_user);

CREATE INDEX idx_reviews_place
    ON reviews(uuid_place);

CREATE INDEX idx_reviews_user
    ON reviews(uuid_user);

CREATE INDEX idx_reviews_status
    ON reviews(status_review);

CREATE INDEX idx_moderation_status
    ON moderation(status_moderation);

CREATE INDEX idx_moderation_place
    ON moderation(uuid_place);

CREATE INDEX idx_complaint_status
    ON complaint(status_complain);

CREATE INDEX idx_photo_place
    ON photo(uuid_place);

CREATE INDEX idx_place_category_place
    ON place_category(uuid_place);

CREATE INDEX idx_place_category_category
    ON place_category(uuid_category);


INSERT INTO categories (uuid_category, category_nm)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'Музей'),
    ('22222222-2222-2222-2222-222222222222', 'Памятник'),
    ('33333333-3333-3333-3333-333333333333', 'Архитектура'),
    ('44444444-4444-4444-4444-444444444444', 'Парк'),
    ('55555555-5555-5555-5555-555555555555', 'Историческое место');


INSERT INTO users (
    uuid_user,
    user_nm,
    role,
    auth_provider,
    login,
    password_hash,
    user_rating,
    moderator_rating
)
VALUES (
    '00000000-0000-0000-0000-000000000000',
    'system',
    'admin',
    'local',
    'system',
    'not_used',
    0,
    0
);