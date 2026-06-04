from sqlalchemy import (
    Column,
    String,
    Text,
    Integer,
    Boolean,
    DateTime,
    Numeric,
    ForeignKey,
)
from sqlalchemy.orm import declarative_base
from sqlalchemy.sql import func


Base = declarative_base()


class Place(Base):
    __tablename__ = "places"

    uuid_place = Column(String(36), primary_key=True)
    place_nm = Column(String(300), nullable=False)
    brief_description = Column(Text, nullable=False)

    # В вашей таблице поле написано historical_bacground.
    # Оставляем такое имя, чтобы совпадало с физической моделью БД.
    historical_bacground = Column(Text, nullable=False)

    audio = Column(Text, nullable=True)
    address = Column(String(300), nullable=True)
    locality = Column(String(60), nullable=False)

    latitude = Column(Numeric(9, 6), nullable=False)
    longitude = Column(Numeric(9, 6), nullable=False)

    created_at = Column(DateTime, nullable=False, server_default=func.now())
    created_uuid_user = Column(String(36), nullable=False)

    likes_place = Column(Integer, nullable=False, default=0)
    rating = Column(Integer, nullable=False, default=0)

    status_place = Column(String(40), nullable=False)


class Review(Base):
    __tablename__ = "reviews"

    uuid_reviews = Column(String(36), primary_key=True)
    uuid_user = Column(String(36), nullable=False)
    uuid_place = Column(String(36), nullable=False)

    photo = Column(Text, nullable=True)
    text = Column(Text, nullable=False)
    grade = Column(Integer, nullable=True)

    created_at = Column(DateTime, nullable=False, server_default=func.now())
    likes_reviews = Column(Integer, nullable=False, default=0)

    is_interesting_fact = Column(Boolean, nullable=False, default=False)


class PlaceCategory(Base):
    __tablename__ = "place_category"

    uuid_category = Column(String(36), primary_key=True)
    uuid_place = Column(String(36), primary_key=True)


class Photo(Base):
    __tablename__ = "photo"

    uuid_photo = Column(String(36), primary_key=True)
    num_photo = Column(Integer, nullable=False)
    url_photo = Column(Text, nullable=False)
    uuid_place = Column(String(36), nullable=False)


class Moderation(Base):
    __tablename__ = "moderation"

    uuid_moderation = Column(String(36), primary_key=True)
    uuid_place = Column(String(36), nullable=False)
    uuid_user_creator = Column(String(36), nullable=False)

    # В вашей таблице поле обязательное, но на этапе создания модератор ещё не назначен.
    # Если в БД стоит NOT NULL, лучше либо сделать nullable,
    # либо записывать системный UUID модератора.
    uuid_user_moderator = Column(String(36), nullable=False)

    comment = Column(String(36), nullable=False)
    status_moderation = Column(String(20), nullable=False)

    checked_at = Column(DateTime, nullable=True)