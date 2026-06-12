from pydantic import BaseModel
from typing import Optional
from datetime import datetime

class User(BaseModel):
    uuid_user: str
    user_nm: str
    role: str
    login: Optional[str] = None
    user_rating: int = 0
    moderator_rating: int = 0

class Place(BaseModel):
    uuid_place: str
    place_nm: str
    brief_description: str
    historical_background: Optional[str] = None
    latitude: float
    longitude: float
    locality: str
    address: Optional[str] = None
    status_place: str
    created_at: datetime
    rating: int = 0
    likes_place: int = 0

class Achievement(BaseModel):
    uuid_achievement: str
    achievement_nm: str
    description: str
    icon: str
    reward_points: int

class Exercise(BaseModel):
    uuid_exercise: str
    exercise_nm: str
    description: str
    target_value: int
    category: str
    is_active: bool

class Complaint(BaseModel):
    uuid_complaint: str
    chapter: str
    uuid_place: Optional[str] = None
    uuid_reviews: Optional[str] = None
    uuid_creator: str
    complaint_reason: str
    status_complain: str
    create_at: datetime