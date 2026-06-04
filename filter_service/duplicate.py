import math
import re
from typing import Optional, List, Dict, Any

import numpy as np
from navec import Navec
from razdel import tokenize
from rapidfuzz import fuzz

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.config import settings
from app.models import Review, Place


def normalize_text(text: Optional[str]) -> str:
    if not text:
        return ""

    text = text.lower()
    text = text.replace("ё", "е")
    text = re.sub(r"[^a-zа-я0-9\s]", " ", text, flags=re.IGNORECASE)
    text = re.sub(r"\s+", " ", text).strip()
    return text


class NavecEmbedder:
    def __init__(self, path: str):
        self.navec = Navec.load(path)

    def text_to_vector(self, text: str) -> Optional[np.ndarray]:
        tokens = [
            token.text.lower().replace("ё", "е")
            for token in tokenize(text)
        ]

        vectors = []

        for token in tokens:
            if token in self.navec:
                vectors.append(self.navec[token])

        if not vectors:
            return None

        return np.mean(vectors, axis=0)

    def similarity(self, text_1: str, text_2: str) -> float:
        vector_1 = self.text_to_vector(text_1)
        vector_2 = self.text_to_vector(text_2)

        if vector_1 is None or vector_2 is None:
            return 0.0

        denominator = np.linalg.norm(vector_1) * np.linalg.norm(vector_2)

        if denominator == 0:
            return 0.0

        score = float(np.dot(vector_1, vector_2) / denominator)
        return max(0.0, min(score, 1.0))


class DuplicateFilter:
    def __init__(self):
        self.embedder = NavecEmbedder(settings.navec_path)

    async def check_review_duplicate(
        self,
        db: AsyncSession,
        uuid_place: str,
        text: str,
    ) -> Dict[str, Any]:
        stmt = (
            select(Review)
            .where(Review.uuid_place == uuid_place)
            .limit(200)
        )

        result = await db.execute(stmt)
        reviews = result.scalars().all()

        best_score = 0.0
        best_review_uuid = None

        for review in reviews:
            score = self.embedder.similarity(text, review.text)

            if score > best_score:
                best_score = score
                best_review_uuid = review.uuid_reviews

        if best_score >= settings.duplicate_threshold:
            decision = "likely_duplicate"
        elif best_score >= settings.possible_duplicate_threshold:
            decision = "possible_duplicate"
        else:
            decision = "not_duplicate"

        return {
            "duplicate_score": round(best_score, 4),
            "decision": decision,
            "matched_review_uuid": best_review_uuid,
        }

    async def check_place_duplicate(
        self,
        db: AsyncSession,
        place_nm: str,
        brief_description: str,
        historical_background: str,
        address: str,
        locality: str,
        latitude: float,
        longitude: float,
    ) -> Dict[str, Any]:
        stmt = (
            select(Place)
            .where(Place.locality == locality)
            .limit(300)
        )

        result = await db.execute(stmt)
        places = result.scalars().all()

        best = None

        for place in places:
            distance_m = self._haversine_distance_m(
                latitude,
                longitude,
                float(place.latitude),
                float(place.longitude),
            )

            name_similarity = self._string_similarity(place_nm, place.place_nm)
            address_similarity = self._string_similarity(address or "", place.address or "")
            geo_similarity = self._geo_similarity(distance_m)

            new_description = f"{brief_description} {historical_background}"
            old_description = f"{place.brief_description} {place.historical_bacground}"

            description_similarity = self.embedder.similarity(
                new_description,
                old_description,
            )

            locality_match = 1.0 if normalize_text(locality) == normalize_text(place.locality) else 0.0

            duplicate_score = (
                0.30 * name_similarity
                + 0.25 * geo_similarity
                + 0.15 * address_similarity
                + 0.20 * description_similarity
                + 0.10 * locality_match
            )

            candidate = {
                "matched_place_uuid": place.uuid_place,
                "matched_place_name": place.place_nm,
                "duplicate_score": round(duplicate_score, 4),
                "name_similarity": round(name_similarity, 4),
                "address_similarity": round(address_similarity, 4),
                "geo_similarity": round(geo_similarity, 4),
                "description_similarity": round(description_similarity, 4),
                "locality_match": locality_match,
                "distance_m": round(distance_m, 2),
            }

            if best is None or duplicate_score > best["duplicate_score"]:
                best = candidate

        if best is None:
            return {
                "duplicate_score": 0.0,
                "decision": "not_duplicate",
                "matched_place_uuid": None,
            }

        score = best["duplicate_score"]

        if score >= settings.duplicate_threshold:
            decision = "likely_duplicate"
        elif score >= settings.possible_duplicate_threshold:
            decision = "possible_duplicate"
        else:
            decision = "not_duplicate"

        best["decision"] = decision
        return best

    def _string_similarity(self, text_1: str, text_2: str) -> float:
        text_1 = normalize_text(text_1)
        text_2 = normalize_text(text_2)

        if not text_1 or not text_2:
            return 0.0

        return fuzz.token_sort_ratio(text_1, text_2) / 100.0

    def _geo_similarity(self, distance_m: float, max_distance_m: float = 300.0) -> float:
        if distance_m <= 0:
            return 1.0

        return max(0.0, 1.0 - min(distance_m / max_distance_m, 1.0))

    def _haversine_distance_m(
        self,
        lat1: float,
        lon1: float,
        lat2: float,
        lon2: float,
    ) -> float:
        radius_m = 6371000

        phi1 = math.radians(lat1)
        phi2 = math.radians(lat2)

        delta_phi = math.radians(lat2 - lat1)
        delta_lambda = math.radians(lon2 - lon1)

        a = (
            math.sin(delta_phi / 2) ** 2
            + math.cos(phi1)
            * math.cos(phi2)
            * math.sin(delta_lambda / 2) ** 2
        )

        c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))

        return radius_m * c