from typing import Dict, Any, List, Optional
from uuid import uuid4

from sqlalchemy.ext.asyncio import AsyncSession

from app.config import settings
from app.filters import (
    TextValidationFilter,
    TechnicalRegexFilter,
    BadWordsFilter,
    ToxicityFilter,
    clean_text,
)
from app.duplicate import DuplicateFilter
from app.schemas import CheckReviewRequest, ContentType
from app.models import Review, Place, Photo, PlaceCategory, Moderation


class ReviewCheckService:
    def __init__(self):
        self.text_validation_filter = TextValidationFilter()
        self.technical_filter = TechnicalRegexFilter()
        self.bad_words_filter = BadWordsFilter()
        self.toxicity_filter = ToxicityFilter()
        self.duplicate_filter = DuplicateFilter()

    async def check(self, db: AsyncSession, request: CheckReviewRequest) -> Dict[str, Any]:
        if request.content_type == ContentType.REVIEW:
            return await self._check_review(db, request)

        if request.content_type == ContentType.PLACE:
            return await self._check_place(db, request)

        raise ValueError("Unknown content_type")

    async def _check_review(
        self,
        db: AsyncSession,
        request: CheckReviewRequest,
    ) -> Dict[str, Any]:
        if not request.text:
            return self._invalid_response(
                violations=["empty_text"],
                cleaned_text="",
            )

        if not request.uuid_place:
            return self._invalid_response(
                violations=["uuid_place_required"],
                cleaned_text=clean_text(request.text),
            )

        text = clean_text(request.text)

        validation_result = self.text_validation_filter.check(text)
        technical_result = self.technical_filter.check(text)
        badwords_result = self.bad_words_filter.check(text)
        toxicity_result = self.toxicity_filter.check(text)

        duplicate_result = await self.duplicate_filter.check_review_duplicate(
            db=db,
            uuid_place=request.uuid_place,
            text=text,
        )

        violations = self._collect_violations(
            validation_result=validation_result,
            technical_result=technical_result,
            badwords_result=badwords_result,
            toxicity_result=toxicity_result,
            duplicate_result=duplicate_result,
        )

        is_valid = len(violations) == 0

        saved_uuid = None

        if is_valid and request.save_if_valid:
            saved_uuid = await self._save_review(db, request, text)

        return {
            "is_valid": is_valid,
            "status": "valid" if is_valid else "invalid",
            "violations": violations,
            "scores": {
                "spam_score": technical_result["spam_score"],
                "toxicity_score": toxicity_result["toxicity_score"],
                "duplicate_score": duplicate_result["duplicate_score"],
            },
            "cleaned_text": text,
            "flags": {
                "technical_filter": technical_result,
                "bad_words": badwords_result,
                "toxicity": toxicity_result,
                "duplicates": duplicate_result,
            },
            "saved_uuid": saved_uuid,
        }

    async def _check_place(
        self,
        db: AsyncSession,
        request: CheckReviewRequest,
    ) -> Dict[str, Any]:
        required_errors = self._validate_place_required_fields(request)

        all_text = clean_text(
            " ".join(
                [
                    request.place_nm or "",
                    request.address or "",
                    request.locality or "",
                    request.brief_description or "",
                    request.historical_background or "",
                ]
            )
        )

        if required_errors:
            return self._invalid_response(
                violations=required_errors,
                cleaned_text=all_text,
            )

        validation_result = self.text_validation_filter.check(all_text)
        technical_result = self.technical_filter.check(all_text)
        badwords_result = self.bad_words_filter.check(all_text)
        toxicity_result = self.toxicity_filter.check(all_text)

        duplicate_result = await self.duplicate_filter.check_place_duplicate(
            db=db,
            place_nm=request.place_nm,
            brief_description=request.brief_description,
            historical_background=request.historical_background,
            address=request.address or "",
            locality=request.locality,
            latitude=request.latitude,
            longitude=request.longitude,
        )

        violations = self._collect_violations(
            validation_result=validation_result,
            technical_result=technical_result,
            badwords_result=badwords_result,
            toxicity_result=toxicity_result,
            duplicate_result=duplicate_result,
        )

        # Для места вероятный дубль не блокируем окончательно,
        # а отправляем на модерацию.
        hard_violations = [
            violation
            for violation in violations
            if violation not in {"possible_duplicate", "likely_duplicate"}
        ]

        is_valid = len(hard_violations) == 0

        saved_uuid = None

        if is_valid and request.save_if_valid:
            saved_uuid = await self._save_place_to_moderation(db, request)

        return {
            "is_valid": is_valid,
            "status": "pending_moderation" if is_valid else "invalid",
            "violations": violations,
            "scores": {
                "spam_score": technical_result["spam_score"],
                "toxicity_score": toxicity_result["toxicity_score"],
                "duplicate_score": duplicate_result["duplicate_score"],
            },
            "cleaned_text": all_text,
            "flags": {
                "technical_filter": technical_result,
                "bad_words": badwords_result,
                "toxicity": toxicity_result,
                "duplicates": duplicate_result,
            },
            "saved_uuid": saved_uuid,
        }

    def _validate_place_required_fields(self, request: CheckReviewRequest) -> List[str]:
        errors = []

        if not request.place_nm or not request.place_nm.strip():
            errors.append("place_nm_required")

        if not request.brief_description or len(request.brief_description.strip()) < 20:
            errors.append("brief_description_too_short")

        if not request.historical_background or len(request.historical_background.strip()) < 20:
            errors.append("historical_background_too_short")

        if not request.locality or not request.locality.strip():
            errors.append("locality_required")

        if request.latitude is None:
            errors.append("latitude_required")

        if request.longitude is None:
            errors.append("longitude_required")

        if not request.categories:
            errors.append("category_required")

        if not request.photos:
            errors.append("photo_required")

        return errors

    def _collect_violations(
        self,
        validation_result: Dict[str, Any],
        technical_result: Dict[str, Any],
        badwords_result: Dict[str, Any],
        toxicity_result: Dict[str, Any],
        duplicate_result: Dict[str, Any],
    ) -> List[str]:
        violations = []

        violations.extend(validation_result.get("violations", []))

        if technical_result["has_url"]:
            violations.append("url_detected")

        if technical_result["has_email"]:
            violations.append("email_detected")

        if technical_result["has_phone"]:
            violations.append("phone_detected")

        if technical_result["has_promo"]:
            violations.append("promo_detected")

        if technical_result["excessive_caps"]:
            violations.append("excessive_caps")

        if technical_result["repeated_chars"]:
            violations.append("repeated_chars")

        if technical_result["spam_score"] >= settings.spam_reject_threshold:
            violations.append("high_spam_score")

        if badwords_result["has_profanity"]:
            violations.append("profanity_detected")

        if toxicity_result["decision"] == "high_toxicity":
            violations.append("high_toxicity")

        if toxicity_result["decision"] == "medium_toxicity":
            violations.append("medium_toxicity")

        if duplicate_result["decision"] == "likely_duplicate":
            violations.append("likely_duplicate")

        if duplicate_result["decision"] == "possible_duplicate":
            violations.append("possible_duplicate")

        return list(dict.fromkeys(violations))

    async def _save_review(
        self,
        db: AsyncSession,
        request: CheckReviewRequest,
        text: str,
    ) -> str:
        uuid_reviews = str(uuid4())

        review = Review(
            uuid_reviews=uuid_reviews,
            uuid_user=request.uuid_user,
            uuid_place=request.uuid_place,
            photo=request.photo,
            text=text,
            grade=request.grade,
            likes_reviews=0,
            is_interesting_fact=request.is_interesting_fact,
        )

        db.add(review)
        await db.commit()

        return uuid_reviews

    async def _save_place_to_moderation(
        self,
        db: AsyncSession,
        request: CheckReviewRequest,
    ) -> str:
        uuid_place = str(uuid4())

        place = Place(
            uuid_place=uuid_place,
            place_nm=request.place_nm,
            brief_description=request.brief_description,
            historical_bacground=request.historical_background,
            audio=None,
            address=request.address,
            locality=request.locality,
            latitude=request.latitude,
            longitude=request.longitude,
            created_uuid_user=request.uuid_user,
            likes_place=0,
            rating=0,
            status_place="на модерации",
        )

        db.add(place)

        for uuid_category in request.categories:
            db.add(
                PlaceCategory(
                    uuid_category=uuid_category,
                    uuid_place=uuid_place,
                )
            )

        for index, url_photo in enumerate(request.photos, start=1):
            db.add(
                Photo(
                    uuid_photo=str(uuid4()),
                    num_photo=index,
                    url_photo=url_photo,
                    uuid_place=uuid_place,
                )
            )

        moderation = Moderation(
            uuid_moderation=str(uuid4()),
            uuid_place=uuid_place,
            uuid_user_creator=request.uuid_user,

            # Лучше заменить на реальный UUID системного модератора,
            # если поле в БД NOT NULL.
            uuid_user_moderator="00000000-0000-0000-0000-000000000000",

            comment="Автоматическая проверка пройдена",
            status_moderation="new",
            checked_at=None,
        )

        db.add(moderation)

        await db.commit()

        return uuid_place

    def _invalid_response(
        self,
        violations: List[str],
        cleaned_text: str,
    ) -> Dict[str, Any]:
        return {
            "is_valid": False,
            "status": "invalid",
            "violations": violations,
            "scores": {
                "spam_score": 0.0,
                "toxicity_score": 0.0,
                "duplicate_score": 0.0,
            },
            "cleaned_text": cleaned_text,
            "flags": {},
            "saved_uuid": None,
        }