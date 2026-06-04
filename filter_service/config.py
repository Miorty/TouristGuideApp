from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    database_url: str = "postgresql+asyncpg://postgres:postgres@localhost:5432/places_db"

    toxicity_model_name: str = "cointegrated/rubert-tiny-toxicity"

    navec_path: str = "navec_hudlit_v1_12B_500K_300d_100q.tar"

    duplicate_threshold: float = 0.85
    possible_duplicate_threshold: float = 0.70

    spam_reject_threshold: float = 0.85
    spam_moderation_threshold: float = 0.50

    toxicity_reject_threshold: float = 0.85
    toxicity_moderation_threshold: float = 0.60

    class Config:
        env_file = ".env"


settings = Settings()