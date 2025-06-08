import os
import pandas as pd
from tqdm import tqdm

from gpt.categorizer import group_by_token_limit, categorize_batch
from contents.contents_generator import content_recommendation
from contents.contents_analyze import save_contents_statistics
from sentiment.analyze import predict_sentiment
from db.insert_comment import (
    insert_category,
    insert_sentiment,
    get_categories_for_video,
    get_comments_by_video_and_category,
    reverse_category_map,
    save_contents_to_db,
)
from summary.summarize_by_category import summarize_category
from summary.save_summary import save_summary_to_db
from db.insert_comment import test_db_connection
from db.fetch_data import (
    get_recent_summaries,
    get_recent_comments
)


# ====== 설정 ======
do_categorization = False     # 댓글 긍부정, 범주화 및 저장
do_summary = False           # 댓글 요약 생성
do_recommendation = False     # 콘텐츠 추천
video_id = 35                  # 대상 영상 ID
user_id = 2                    # 콘텐츠 추천용 유저 ID
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

# DB 연결 확인
test_db_connection()

# ====== 댓글 가져오기 ======
if do_categorization:
    print(f"\nvideo_id={video_id} 댓글 불러오는 중...")
    df = get_recent_comments(video_id)

    if df.empty:
        print(f"{video_id}에 해당하는 분류되지 않은 댓글이 없습니다.")
    else:
        # ====== 감정 분석 전처리 ======
        print("[INFO] 감정 분석 시작")
        df_sentiment = df.copy()
        comment_list = df_sentiment["comment"].dropna().tolist()
        print(f"[INFO] 감정 분석 대상 댓글 수: {len(comment_list)}")

        sentiments = predict_sentiment(comment_list)
        print(f"[INFO] 감정 분석 완료. 결과 수: {len(sentiments)}")

        df_sentiment = df_sentiment.loc[:len(sentiments) - 1].copy()
        df_sentiment["is_positive"] = [1 if s == "positive" else 0 for s in sentiments]


        # ====== 댓글 범주화 (원본 댓글 기준) ======
        df_category = df.copy()
        comments = df_category["comment"].dropna().tolist()
        batches = group_by_token_limit(comments)
        print(f"[INFO] GPT 요청 예정 댓글 수: {len(comments)}")
        print(f"[INFO] GPT 요청 배치 수: {len(batches)}")

        results = []
        for i, batch in enumerate(tqdm(batches, desc="GPT 범주화 중")):
            print(f"[INFO] GPT 요청 중: Batch {i+1}/{len(batches)}, 크기: {len(batch)}")
            categorized = categorize_batch(batch)
            print(f"[INFO] GPT 응답 수: {len(categorized)}")
            results.extend(categorized[:len(batch)])

        print(f"[INFO] GPT 범주화 전체 결과 수: {len(results)}")

        df_category = df_category.loc[:len(results) - 1].copy()
        df_category["Category"] = results
        df_category["category_id"] = df_category["Category"].str.lower().map(category_map)

        unmapped_count = df_category["category_id"].isna().sum()
        if unmapped_count > 0:
            print(f"[WARNING] 범주 매핑되지 않은 항목 수: {unmapped_count}")


        # ====== DB 업데이트 ======
        insert_category(df_category)
        insert_sentiment(df_sentiment)

# ====== 댓글 요약 생성 ======
if do_summary:
    category_ids = get_categories_for_video(video_id)
    category_ids = [cid for cid in category_ids if cid is not None]

    print(f"\n감지된 category_ids: {category_ids}")

    for category_id in category_ids:
        category_name = reverse_category_map.get(category_id, f"unknown_{category_id}")
        print(f"\n요약 생성 중: {category_name}")
        summary = summarize_category(video_id, category_name)
        print(f"요약 결과 ({category_name}):\n{summary}\n")
        save_summary_to_db(video_id, category_name, summary)


# ====== 콘텐츠 추천 ======
if do_recommendation:
    save_contents_statistics(user_id) # 통계
    content_recommendation(user_id) # 콘텐츠 추천


