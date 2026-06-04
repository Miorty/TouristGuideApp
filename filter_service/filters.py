import re
import string
from typing import Dict, Any, List

import torch
from transformers import pipeline

from badwords import ProfanityFilter

from app.config import settings


def is_only_punctuation(text: str) -> bool:
    text = text.strip()

    if not text:
        return True

    punctuation = set(string.punctuation + "—–«»…№.,!?;:()[]{}")
    return all(ch in punctuation or ch.isspace() for ch in text)


def has_meaningful_chars(text: str) -> bool:
    return bool(re.search(r"[a-zA-Zа-яА-Я0-9]", text))


def clean_text(text: str) -> str:
    text = text.strip()
    text = re.sub(r"\s+", " ", text)
    return text


class TechnicalRegexFilter:
    URL_PATTERN = re.compile(
        r"""
        (
            https?://[^\s]+
            |
            www\.[^\s]+
            |
            [a-zA-Z0-9.-]+\.(ru|com|net|org|info|biz|рф|io|me|site|online)\b
        )
        """,
        re.IGNORECASE | re.VERBOSE,
    )

    EMAIL_PATTERN = re.compile(
        r"\b[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Zа-яА-Я]{2,}\b",
        re.IGNORECASE,
    )

    PHONE_PATTERN = re.compile(
        r"""
        (
            (\+7|8)[\s\-()]*
            \d{3}[\s\-()]*
            \d{3}[\s\-()]*
            \d{2}[\s\-()]*
            \d{2}
        )
        """,
        re.VERBOSE,
    )

    PROMO_PATTERN = re.compile(
        r"\b(промокод|скидка|купон|акция|sale|promo|discount|закажи|переходи|подпишись|реклама)\b",
        re.IGNORECASE,
    )

    REPEATED_CHARS_PATTERN = re.compile(r"(.)\1{4,}", re.IGNORECASE)
    REPEATED_PUNCTUATION_PATTERN = re.compile(r"([!?.,])\1{3,}")

    def check(self, text: str) -> Dict[str, Any]:
        has_url = bool(self.URL_PATTERN.search(text))
        has_email = bool(self.EMAIL_PATTERN.search(text))
        has_phone = bool(self.PHONE_PATTERN.search(text))
        has_promo = bool(self.PROMO_PATTERN.search(text))
        repeated_chars = bool(self.REPEATED_CHARS_PATTERN.search(text))
        repeated_punctuation = bool(self.REPEATED_PUNCTUATION_PATTERN.search(text))
        excessive_caps = self._has_excessive_caps(text)

        spam_score = 0.0

        if has_url:
            spam_score += 0.30
        if has_email:
            spam_score += 0.25
        if has_phone:
            spam_score += 0.25
        if has_promo:
            spam_score += 0.20
        if excessive_caps:
            spam_score += 0.15
        if repeated_chars:
            spam_score += 0.10
        if repeated_punctuation:
            spam_score += 0.10

        spam_score = min(spam_score, 1.0)

        return {
            "has_url": has_url,
            "has_email": has_email,
            "has_phone": has_phone,
            "has_promo": has_promo,
            "excessive_caps": excessive_caps,
            "repeated_chars": repeated_chars,
            "repeated_punctuation": repeated_punctuation,
            "spam_score": round(spam_score, 4),
        }

    def _has_excessive_caps(self, text: str) -> bool:
        letters = [ch for ch in text if ch.isalpha()]

        if len(letters) < 20:
            return False

        upper_count = sum(1 for ch in letters if ch.isupper())
        return upper_count / len(letters) >= 0.7


class BadWordsFilter:
    def __init__(self):
        self.filter = ProfanityFilter()
        self.filter.init(languages=["ru", "en"])

    def check(self, text: str) -> Dict[str, Any]:
        """
        Метод filter_text возвращает обработанный текст.
        Если после фильтрации текст изменился, значит была найдена нецензурная лексика.
        """
        try:
            filtered_text = self.filter.filter_text(text, replace_character="*")
        except TypeError:
            # На случай другой версии API.
            filtered_text = self.filter.filter_text(text)

        has_profanity = filtered_text != text

        return {
            "has_profanity": has_profanity,
            "filtered_text": filtered_text,
        }


class ToxicityFilter:
    def __init__(self):
        device = 0 if torch.cuda.is_available() else -1

        self.model = pipeline(
            task="text-classification",
            model=settings.toxicity_model_name,
            tokenizer=settings.toxicity_model_name,
            top_k=None,
            device=device,
        )

    def check(self, text: str) -> Dict[str, Any]:
        chunks = self._split_text(text, max_len=800)

        all_scores: List[Dict[str, float]] = []
        max_toxicity = 0.0

        for chunk in chunks:
            result = self.model(chunk)
            parsed = self._parse_result(result)

            all_scores.append(parsed)

            chunk_toxicity = max(
                parsed.get("toxic", 0.0),
                parsed.get("toxicity", 0.0),
                parsed.get("insult", 0.0),
                parsed.get("obscene", 0.0),
                parsed.get("threat", 0.0),
            )

            max_toxicity = max(max_toxicity, chunk_toxicity)

        if max_toxicity >= settings.toxicity_reject_threshold:
            decision = "high_toxicity"
        elif max_toxicity >= settings.toxicity_moderation_threshold:
            decision = "medium_toxicity"
        else:
            decision = "passed"

        return {
            "toxicity_score": round(max_toxicity, 4),
            "decision": decision,
            "chunks": all_scores,
        }

    def _split_text(self, text: str, max_len: int) -> List[str]:
        text = text.strip()

        if len(text) <= max_len:
            return [text]

        sentences = re.split(r"(?<=[.!?])\s+", text)
        chunks = []
        current = ""

        for sentence in sentences:
            if len(current) + len(sentence) <= max_len:
                current = f"{current} {sentence}".strip()
            else:
                if current:
                    chunks.append(current)
                current = sentence

        if current:
            chunks.append(current)

        return chunks

    def _parse_result(self, result) -> Dict[str, float]:
        if isinstance(result, list) and result and isinstance(result[0], list):
            result = result[0]

        scores = {}

        for item in result:
            label = str(item["label"]).lower()
            score = float(item["score"])
            scores[label] = score

        return scores


class TextValidationFilter:
    def check(self, text: str) -> Dict[str, Any]:
        violations = []

        if not text or not text.strip():
            violations.append("empty_text")

        if text and is_only_punctuation(text):
            violations.append("only_punctuation")

        if text and not has_meaningful_chars(text):
            violations.append("no_meaningful_chars")

        if len(text) > 5000:
            violations.append("text_too_long")

        return {
            "is_valid": not violations,
            "violations": violations,
        }