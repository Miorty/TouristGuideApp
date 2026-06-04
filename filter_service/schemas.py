from enum import Enum
from typing import Optional, List, Dict, Any
from pydantic import BaseModel, Field, field_validator


class ContentType(str, Enum):
    REVIEW = "review"
    PLACE = "place"


class CheckReviewRequest(BaseModel):
    content_type: ContentType = Field(default=ContentType.REVIEW)

    uuid_user: str = Field(..., min_length=36, max_length=36)
    uuid_place: Optional[str] = Field(default=None, min_length=36, max_length=36)

    # Для review.
    text: Optional[str] = Field(default=None, min_length=1, max_length=5000)
    grade: Optional[int] = Field(default=None, ge=1, le=5)
    is_interesting_fact: bool = False
    photo: Optional[str] = None

    # Для place.
    place_nm: Optional[str] = Field(default=None, max_length=300)
    brief_description: Optional[str] = Field(default=None, max_length=5000)
    historical_background: Optional[str] = Field(default=None, max_length=5000)
    address: Optional[str] = Field(default=None, max_length=300)
    locality: Optional[str] = Field(default=None, max_length=60)
    latitude: Optional[float] = Field(default=None, ge=-90, le=90)
    longitude: Optional[float] = Field(default=None, ge=-180, le=180)
    categories: List[str] = Field(default_factory=list)
    photos: List[str] = Field(default_factory=list)

    save_if_valid: bool = True

    @field_validator("text")
    @classmethod
    def validate_text(cls, value):
        if value is not None and not value.strip():
            raise ValueError("Текст не может быть пустым")
        return value


class CheckReviewResponse(BaseModel):
    is_valid: bool
    status: str
    violations: List[str]
    scores: Dict[str, Any]
    cleaned_text: str
    flags: Dict[str, Any]
    saved_uuid: Optional[str] = None