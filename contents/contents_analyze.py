import pandas as pd
import re
from sqlalchemy import create_engine,text
from config import DB_URL, get_connection
from sentiment.analyze import predict_sentiment
from datetime import datetime,timedelta
import json
from collections import Counter

import sys
import os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

# 콘텐츠 기간 설정 함수
def get_last_month_period_str():
    today = datetime.today()
    first_day_this_month = today.replace(day=1)
    last_month_end = first_day_this_month - timedelta(days=1)
    last_month_start = last_month_end.replace(day=1)
    return f"{last_month_start.strftime('%Y.%m.%d')}~{last_month_end.strftime('%Y.%m.%d')}"

def save_contents_statistics(user_id: int):
    engine = create_engine(DB_URL)
    now = datetime.now().strftime("%Y-%m-%d")
    video_period_str = get_last_month_period_str()

    with engine.connect() as conn:
        # ===== 1. 최고 조회수 영상 =====
        view_query = """
            SELECT v.title, v.thumbnail, vm.views
            FROM video v
            JOIN video_meta vm ON v.video_id = vm.video_id
            WHERE v.user_id = :user_id
              AND v.upload_date >= DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 MONTH), '%Y-%m-01')
              AND v.upload_date < DATE_FORMAT(CURDATE(), '%Y-%m-01')
            ORDER BY vm.views DESC
            LIMIT 1;
        """
        view_result = conn.execute(text(view_query), {"user_id": user_id}).fetchone()
        top_view_video = json.dumps(dict(zip(["title", "thumbnail", "views"], view_result)), ensure_ascii=False) if view_result else None

        # ===== 2. 긍/부정 댓글 비율 최고 영상 =====
        senti_query = text("""
            SELECT v.title, v.thumbnail,
                   SUM(CASE WHEN s.is_positive = 1 THEN s.count ELSE 0 END) AS positive_count,
                   SUM(CASE WHEN s.is_positive = 0 THEN s.count ELSE 0 END) AS negative_count
            FROM senti_stat s
            JOIN video v ON s.video_id = v.video_id
            WHERE v.user_id = :user_id
              AND v.upload_date >= DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 MONTH), '%Y-%m-01')
              AND v.upload_date < DATE_FORMAT(CURDATE(), '%Y-%m-01')
            GROUP BY v.video_id, v.title, v.thumbnail
        """)
        rows = conn.execute(senti_query, {"user_id": user_id}).fetchall()

        max_pos_ratio, max_neg_ratio = -1, -1
        top_pos_video, top_neg_video = None, None

        for row in rows:
            total = row.positive_count + row.negative_count
            if total == 0:
                continue
            pos_ratio = row.positive_count / total
            neg_ratio = row.negative_count / total

            if pos_ratio > max_pos_ratio:
                max_pos_ratio = pos_ratio
                top_pos_video = row

            if neg_ratio > max_neg_ratio:
                max_neg_ratio = neg_ratio
                top_neg_video = row

        top_positive_video = json.dumps({
            "title": top_pos_video.title,
            "thumbnail": top_pos_video.thumbnail,
            "ratio": float(round(max_pos_ratio, 4))
        }, ensure_ascii=False) if top_pos_video else None

        top_negative_video = json.dumps({
            "title": top_neg_video.title,
            "thumbnail": top_neg_video.thumbnail,
            "ratio": float(round(max_neg_ratio, 4))
        }, ensure_ascii=False) if top_neg_video else None

        # ===== 3. 긍정 키워드 TOP 5 =====
        keyword_query = text("""
            SELECT s.keywords
            FROM senti_stat s
            JOIN video v ON s.video_id = v.video_id
            WHERE v.user_id = :user_id
              AND v.upload_date >= DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 MONTH), '%Y-%m-01')
              AND v.upload_date < DATE_FORMAT(CURDATE(), '%Y-%m-01')
              AND s.is_positive = 1
              AND s.keywords IS NOT NULL
        """)
        keyword_counter = Counter()
        rows = conn.execute(keyword_query, {"user_id": user_id}).fetchall()

        for row in rows:
            try:
                keyword_list = json.loads(row[0])
                if isinstance(keyword_list, list):
                    texts = [item["text"] for item in keyword_list if "text" in item]
                    keyword_counter.update(texts)
            except Exception as e:
                print(f"[WARN] JSON 파싱 실패 → {row[0]} / 오류: {e}")

        top_keywords = [kw for kw, _ in keyword_counter.most_common(5)]
        positive_keywords = json.dumps(top_keywords, ensure_ascii=False) if top_keywords else None

        # ===== 4. 범주 TOP 5 =====
        category_name_map = {
            1: "joy", 2: "supportive", 3: "suggestion", 4: "hate", 5: "question",
            6: "praise", 7: "sympathy", 8: "congratulations", 9: "concern", 10: "other"
        }

        category_query = text("""
            SELECT cs.category_id, SUM(cs.count) AS total
            FROM category_stat cs
            JOIN video v ON cs.video_id = v.video_id
            WHERE v.user_id = :user_id
              AND v.upload_date >= DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 MONTH), '%Y-%m-01')
              AND v.upload_date < DATE_FORMAT(CURDATE(), '%Y-%m-01')
            GROUP BY cs.category_id
            ORDER BY total DESC
            LIMIT 5;
        """)
        rows = conn.execute(category_query, {"user_id": user_id}).fetchall()
        top_categories = [category_name_map.get(row.category_id, f"unknown({row.category_id})") for row in rows]
        top_categories_json = json.dumps(top_categories, ensure_ascii=False) if top_categories else None

        # ===== 5. Insert to DB =====
        insert_stmt = text("""
            INSERT INTO contents (
                video_period, update_date, user_id, 
                top_view_video, top_positive_video, top_negative_video, 
                positive_keywords, top_categories
            )
            VALUES (:video_period, :update_date, :user_id, 
                    :top_view_video, :top_positive_video, :top_negative_video, 
                    :positive_keywords, :top_categories)
        """)

        conn.execute(insert_stmt, {
            "video_period": video_period_str,
            "update_date": now,
            "user_id": user_id,
            "top_view_video": top_view_video,
            "top_positive_video": top_positive_video,
            "top_negative_video": top_negative_video,
            "positive_keywords": positive_keywords,
            "top_categories": top_categories_json
        })

        conn.commit()
        print("[저장 완료] 콘텐츠 통계 저장 완료")