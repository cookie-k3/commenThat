import pandas as pd
from tqdm import tqdm

from .analysis_broker import subscriber_comment, publish_comment

from gpt.categorizer import group_by_token_limit, categorize_batch
from sentiment.analyze import predict_sentiment
from db.insert_comment import (
    insert_category,
    insert_sentiment,
    get_categories_for_video,
    reverse_category_map,
    test_db_connection,
)
from summary.summarize_by_category import summarize_category
from summary.save_summary import save_summary_to_db
from db.fetch_data import get_recent_comments

category_map = {
    "joy": 1, "supportive": 2, "suggestion": 3, "hate": 4, "question": 5,
    "praise": 6, "sympathy": 7, "congratulations": 8, "concern": 9,
    "other": 10, "unknown": None
}

def process_task(video_id):
    try:
        test_db_connection()
        print(f"[SUB]video_id={video_id} 작업 시작")

        df = get_recent_comments(video_id)
        if df.empty:
            print(f"[INFO] video_id={video_id} 댓글 없음.")
            return

        print("[INFO] 감정 분석 시작")
        comments = df["comment"].dropna().tolist()
        sentiments = predict_sentiment(comments)
        df["is_positive"] = [1 if s == "positive" else 0 for s in sentiments]
        insert_sentiment(df)

        print("[INFO] 댓글 범주화 시작")
        batches = group_by_token_limit(comments)
        results = []
        for i, batch in enumerate(tqdm(batches, desc="GPT 범주화 중")):
            categorized = categorize_batch(batch)
            results.extend(categorized[:len(batch)])

        # 결과 개수 안맞을 땐 unknown으로 채우기
        if len(results) < len(df):
            missing = len(df) - len(results)
            print(f"[WARNING] GPT 응답 부족: {len(results)} / {len(df)}. 'unknown'으로 채웁니다.")
            results.extend(["unknown"] * missing)
        elif len(results) > len(df):
            results = results[:len(df)]

        df["Category"] = results
        df["category_id"] = df["Category"].str.lower().map(category_map)
        insert_category(df)


        print("[INFO] 댓글 요약 시작")
        category_ids = get_categories_for_video(video_id)
        for category_id in filter(None, category_ids):
            category_name = reverse_category_map.get(category_id, f"unknown_{category_id}")
            summary = summarize_category(video_id, category_name)
            save_summary_to_db(video_id, category_name, summary)

        # 완료 상태 전송
        publish_comment(video_id)

    except Exception as e:
        print(f"[ERROR] 처리 중 오류: {e}")

if __name__ == "__main__":
    subscriber_comment(process_task)