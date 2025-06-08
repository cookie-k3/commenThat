import pandas as pd
import re
from sqlalchemy import create_engine,text
from config import DB_URL, get_connection
from sentiment.analyze import predict_sentiment
from datetime import datetime,timedelta
import json

category_map = {
    "joy": 1,
    "supportive": 2,
    "suggestion": 3,
    "hate": 4,
    "question": 5,
    "praise": 6,
    "sympathy": 7,
    "congratulations": 8,
    "concern": 9,
    "other": 10,
    "unknown": None
}

reverse_category_map = {v: k for k, v in category_map.items() if v is not None}

def test_db_connection():
    try:
        conn = get_connection()
        with conn.cursor() as cursor:
            cursor.execute("SELECT 1")
            result = cursor.fetchone()
            print("DB 연결 성공:", result)
    except Exception as e:
        print("DB 연결 실패:", e)


# 카테고리 저장
def insert_category(df: pd.DataFrame):
    engine = create_engine(DB_URL)

    update_sql = """
    UPDATE video_comment
    SET category_id = :category_id
    WHERE video_comment_id = :video_comment_id
    """

    with engine.begin() as conn:
        for _, row in df.iterrows():
            if pd.notnull(row["category_id"]) and pd.notnull(row["video_comment_id"]):
                conn.execute(text(update_sql), {
                    "category_id": int(row["category_id"]),
                    "video_comment_id": int(row["video_comment_id"])
                })

    print(f"{len(df)}개의 댓글에 category_id가 업데이트되었습니다.")

# 감정 저장
def insert_sentiment(df: pd.DataFrame):
    engine = create_engine(DB_URL)

    update_sql = """
    UPDATE video_comment
    SET is_positive = :is_positive
    WHERE video_comment_id = :video_comment_id
    """

    with engine.begin() as conn:
        for _, row in df.iterrows():
            if pd.notnull(row["is_positive"]) and pd.notnull(row["video_comment_id"]):
                conn.execute(text(update_sql), {
                    "is_positive": int(row["is_positive"]),
                    "video_comment_id": int(row["video_comment_id"])
                })

    print(f"{len(df)}개의 댓글에 is_positive가 업데이트되었습니다.")

def get_comments_by_video_and_category(video_id, category_name):
    category_id = category_map.get(category_name)
    if category_id is None:
        return []

    connection = get_connection()
    try:
        with connection.cursor() as cursor:
            query = """
                SELECT comment
                FROM video_comment
                WHERE video_id = %s AND category_id = %s
                ORDER BY like_count DESC
            """
            cursor.execute(query, (video_id, category_id))
            return [row["comment"] for row in cursor.fetchall()]
    finally:
        connection.close()

def get_categories_for_video(video_id):
    connection = get_connection()
    try:
        with connection.cursor() as cursor:
            query = """
                SELECT DISTINCT category_id
                FROM video_comment
                WHERE video_id = %s
            """
            cursor.execute(query, (video_id,))
            result = cursor.fetchall()
            return [row["category_id"] for row in result]
    finally:
        connection.close()

# # 자연어 텍스트 출력 시 함수 필요(현재는 json 방식으로 되어 있음)
# #############현재는 사용 안함######################
# def parse_gpt_contents_result(gpt_result: str) -> list[dict]:
#     try:
#         return json.loads(gpt_result.strip())
#     except json.JSONDecodeError as e:
#         print(f"[ERROR] JSON 파싱 실패: {e}")
#         print("[DEBUG] GPT 출력 원본:\n", gpt_result)
#         return []
# ##################################################

def save_contents_to_db(user_id: int, gpt_result: str):
    engine = create_engine(DB_URL)
    now = datetime.now().strftime("%Y-%m-%d")

    try:
        if isinstance(gpt_result, list):
            contents_data = gpt_result
        else:
            json_start = gpt_result.find("[")
            json_raw = gpt_result[json_start:].strip()

            if not json_raw.endswith("]"):
                json_raw += "]"
            json_raw = re.sub(r",\s*]", "]", json_raw)

            contents_data = json.loads(json_raw)

    except Exception as e:
        print(f"[ERROR] GPT JSON 파싱 실패: {e}")
        return

    with engine.connect() as conn:
        for item in contents_data:
            topic = (item.get("topic") or item.get("keyword") or "").strip()
            topic_rec = (item.get("topic_rec") or item.get("guide") or "").strip()
            comment_analysis = (item.get("comment_analysis") or "").strip()

            if not topic or not topic_rec:
                print(f"[WARN] topic 또는 topic_rec 누락 → 건너뜀: {item}")
                continue

            stmt = text("""
            INSERT INTO contents (video_period, update_date, user_id, topic, topic_rec, comment_analysis)
            VALUES (:video_period, :update_date, :user_id, :topic, :topic_rec, :comment_analysis)
            """)
            conn.execute(stmt, {
                "video_period": None,
                "update_date": now,
                "user_id": user_id,
                "topic": topic,
                "topic_rec": topic_rec,
                "comment_analysis": comment_analysis,
            })

            print(f"[저장 완료] topic: {topic}")
            print(f"[가이드 요약]: {topic_rec[:100]}...")
            print(f"[추천 근거]: {comment_analysis[:100]}...\n")


        conn.commit()

    print(f"총 {len(contents_data)}개의 콘텐츠 추천 결과가 저장되었습니다.")

