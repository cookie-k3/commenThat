from sqlalchemy import create_engine, text
import os
from dotenv import load_dotenv
import pandas as pd

load_dotenv()
DB_URL = os.getenv("DB_URL")
engine = create_engine(DB_URL)

# 요약 가지고 오기
def get_recent_summaries(user_id: int) -> list[str]:
    query = """
    SELECT cs.summary
    FROM category_stat cs
    JOIN video v ON cs.video_id = v.video_id
    WHERE v.user_id = :user_id
      AND cs.summary IS NOT NULL
    """
    with engine.connect() as conn:
        result = conn.execute(text(query), {"user_id": user_id})
        return [row[0] for row in result.fetchall()]

#분석 댓글 가져오기
def get_recent_comments(video_id: int) -> pd.DataFrame:
    query = """
    SELECT comment, like_count, upload_date, video_comment_id, video_id
    FROM video_comment
    WHERE video_id = :video_id
      AND is_positive IS NULL
    """
    with engine.connect() as conn:
        result = conn.execute(text(query), {"video_id": video_id})
        df = pd.DataFrame(result.fetchall(), columns=result.keys())
        print(f"전체 분석 대상 댓글 수: {len(df)}")
    return df