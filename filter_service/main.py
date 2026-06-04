from contextlib import asynccontextmanager

from fastapi import FastAPI, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.database import get_db
from app.schemas import CheckReviewRequest, CheckReviewResponse
from app.service import ReviewCheckService


service: ReviewCheckService | None = None


@asynccontextmanager
async def lifespan(app: FastAPI):
    global service

    # Здесь один раз загружаются:
    # regex-фильтр,
    # BadWords,
    # RuBERT,
    # Navec.
    service = ReviewCheckService()

    yield


app = FastAPI(
    title="Text Filtering Service",
    version="1.0.0",
    lifespan=lifespan,
)


@app.post(
    "/api/v1/check_review",
    response_model=CheckReviewResponse,
)
async def check_review(
    request: CheckReviewRequest,
    db: AsyncSession = Depends(get_db),
):
    return await service.check(db, request)


@app.get("/health")
async def health():
    return {
        "status": "ok",
    }