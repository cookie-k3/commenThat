from openai import OpenAI
import os
from gpt.prompt import contents_prompt
from dotenv import load_dotenv
from gpt.token_utils import count_tokens
import json
import re
from datetime import datetime
from sqlalchemy import create_engine, text
from config import DB_URL

load_dotenv()
client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

def compress_category_summaries(summaries, batch_size=15):
    compressed = []
    for i in range(0, len(summaries), batch_size):
        batch = summaries[i:i+batch_size]
        prompt = f"""
다음은 유튜브 영상 댓글 범주별 요약 목록입니다.

조건:
1. 새로운 정보를 추가하지 말고, 주어진 요약에서 **자주 등장하는 주제, 인물, 감정 표현만 사용**하세요.
2. **실제 등장한 인물, 키워드는 반드시 명시**하고, 모호한 표현은 쓰지 마세요.
3. **중복 표현을 제거하고, 3문장 이내로 서술형으로 요약**하세요.
4. 반드시 **입력된 단어 그대로** 사용하세요.

요약 대상 목록:
{chr(10).join(f"- {s}" for s in batch)}
"""
        print(f"[INFO] 중간 요약 요청 (요약 수: {len(batch)})")
        response = client.chat.completions.create(
            model="gpt-4o-mini",
            messages=[{"role": "user", "content": prompt}],
            temperature=0.3,
            max_tokens=500
        )
        compressed_summary = response.choices[0].message.content.strip()
        compressed.append(compressed_summary)
        print(f"[INFO] 중간 요약 완료 (Batch {i//batch_size + 1}):\n{compressed_summary}\n")
    return compressed

def get_content_summary_for_gpt(user_id: int):
    print(f"[INFO] 사용자 ID: {user_id}에 대한 콘텐츠 요약 조회 시작")
    engine = create_engine(DB_URL)
    with engine.connect() as conn:
        # 1. 가장 최신 contents 데이터 조회 (통계 저장된 행)
        latest_content_query = text("""
            SELECT contents_id, top_categories, video_period, top_view_video, top_positive_video, top_negative_video
            FROM contents 
            WHERE user_id = :user_id 
            ORDER BY update_date DESC 
            LIMIT 1
        """)
        latest_content = conn.execute(latest_content_query, {"user_id": user_id}).fetchone()

        if not latest_content:
            raise ValueError("[ERROR] 통계 데이터가 없습니다. 먼저 통계 저장을 진행하세요.")

        top_categories = json.loads(latest_content.top_categories) if latest_content.top_categories else []

        # category_stat 테이블에서 top_categories에 해당하는 summary 추출
        placeholders = ','.join([':cat' + str(i) for i in range(len(top_categories))]) or 'NULL'
        category_params = {f"cat{i}": cat for i, cat in enumerate(top_categories)}
        category_params["user_id"] = user_id


        category_query = text(f"""
            SELECT cs.summary 
            FROM category_stat cs 
            JOIN video v ON cs.video_id = v.video_id 
            WHERE v.user_id = :user_id 
              AND v.upload_date >= DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 MONTH), '%Y-%m-01')
              AND v.upload_date < DATE_FORMAT(CURDATE(), '%Y-%m-01')
              AND cs.category_id IN ({placeholders})
        """)
        category_rows = conn.execute(category_query, category_params).fetchall()
        summaries = [row[0] for row in category_rows]

        # suggestion 범주(카테고리 ID 3)가 포함되지 않으면 추가
        if "suggestion" not in top_categories:
            suggestion_query = text("""
                SELECT cs.summary 
                FROM category_stat cs 
                JOIN video v ON cs.video_id = v.video_id 
                WHERE v.user_id = :user_id 
                  AND cs.category_id = 3 
                  AND v.upload_date >= DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 MONTH), '%Y-%m-01')
                  AND v.upload_date < DATE_FORMAT(CURDATE(), '%Y-%m-01')
            """)
            suggestion_rows = conn.execute(suggestion_query, {"user_id": user_id}).fetchall()
            summaries.extend([row[0] for row in suggestion_rows])

        return summaries, latest_content

def contents_gpt_prompt(category_summaries, summary_result):
    top_view = json.loads(summary_result.top_view_video) if summary_result else {}
    top_positive = json.loads(summary_result.top_positive_video) if summary_result else {}
    top_negative = json.loads(summary_result.top_negative_video) if summary_result else {}

    compressed_summaries = compress_category_summaries(category_summaries)
    combined_summary = "\n".join(compressed_summaries)

    prompt = f"""
당신은 최고의 유튜브 콘텐츠 기획 전문가입니다. 아래 조건을 반드시 준수하여 향후 제작할 유튜브 콘텐츠 주제를 2개 추천하세요.

### 조건:
1. 추천 주제는 반드시 **핵심 키워드 형태로 간결하고 명확하게 작성**하세요. (예: "조개 먹방", "도심 속 캠핑 체험")
2. **가이드**는 다음 사항을 모두 포함하여 줄글로 작성해 주세요:
   - 콘텐츠의 전체적인 흐름 (도입, 본문, 마무리)
   - 촬영 기법 (예: 고화질 클로즈업, 핸드헬드, 드론 촬영 등)
   - 편집 방식 (예: 빠른 템포 편집, 트랜지션 사용, 색보정 방식 등)
   - 자막 스타일 (예: 키워드 강조, 크고 선명한 자막, 재미있는 효과 등)
   - 보조 콘텐츠 아이디어 (예: 관련 브이로그, 비하인드 영상, 챌린지 영상 등)
3. **추천 근거**에는 전달 받은 데이터에 기반하여 다음을 반영하여 줄글로 작성해 주세요:
   - 제공된 최고 조회수, 긍정/부정 비율 최고 영상 제목과 분석
   - 댓글 요약에서 추출한 시청자 반응과 요청 사항
   - 주요 키워드 (예: "신선함", "유익함", "재미" 등)
   - 조회수 및 증가 데이터를 활용한 설득력 있는 분석
4. **형식 (JSON 배열)**:
[
  {{
    "topic": "추천 주제 키워드",
    "guide": "영상 제작 전체에 대한 구체적이고 세부적인 가이드(줄글로 작성)",
    "reason": "추천 근거 (시청자 반응, 요청사항, 조회수 증가 데이터 포함하여 줄글로 작성)"
  }},
  {{
    "topic": "추천 주제 키워드",
    "guide": "...",
    "reason": "..."
  }}
]

[한 달간 범주 요약]
{combined_summary}

[최고 조회수 영상 제목]
{top_view.get('title', '정보 없음')}

[긍정 댓글 비율 최고 영상 제목]
{top_positive.get('title', '정보 없음')}

[부정 댓글 비율 최고 영상 제목]
{top_negative.get('title', '정보 없음')}
"""
    return prompt

def postprocess_gpt_result(raw_output: str):
    try:
        # 코드 블록 마크다운 제거
        cleaned_output = re.sub(r"```json|```", "", raw_output).strip()
        json_start = cleaned_output.find("[")
        json_str = cleaned_output[json_start:]
        return json.loads(json_str)
    except Exception as e:
        print(f"[ERROR] JSON 파싱 실패: {e}")
        return []

def content_recommendation(user_id: int):
    print("[INFO] 콘텐츠 추천 프로세스 시작")
    category_summaries, summary_result = get_content_summary_for_gpt(user_id)
    prompt = contents_gpt_prompt(category_summaries, summary_result)

    print("[INFO] GPT에 콘텐츠 추천 요청")
    response = client.chat.completions.create(
        model="gpt-4o",
        messages=[{"role": "system", "content": "당신은 유튜브 콘텐츠 기획 전문가입니다."},
                  {"role": "user", "content": prompt}],
        temperature=0.7,
        top_p=0.9, #확률이 높은 순서대로 누적 확률이 top_p 이상일 때까지 후보군 생성
        max_tokens=3500,
        timeout=120  # 초 단위 타임아웃
    )

    result = response.choices[0].message.content.strip()
    print("[INFO] GPT 응답 원문:\n", result)
    recommendations = postprocess_gpt_result(result)
    print(f"[INFO] 추천 주제 개수: {len(recommendations)}")


    # 최신 행에 업데이트
    update_stmt = text("""
    UPDATE contents 
    SET topic = :topic, topic_rec = :topic_rec, topic_analysis = :topic_analysis 
    WHERE user_id = :user_id 
    ORDER BY update_date DESC 
    LIMIT 1
""")

    topics = [rec.get("topic") for rec in recommendations]
    topic_recs = [rec.get("guide") for rec in recommendations]
    topic_analyses = [rec.get("reason") for rec in recommendations]

    engine = create_engine(DB_URL)
    with engine.connect() as conn:
        print(f"[INFO] 저장할 추천 주제 목록:\n{json.dumps(topics, ensure_ascii=False)}")
        conn.execute(update_stmt, {
            "user_id": user_id,
            "topic": json.dumps(topics, ensure_ascii=False),
            "topic_rec": json.dumps(topic_recs, ensure_ascii=False),
            "topic_analysis": json.dumps(topic_analyses, ensure_ascii=False)
        })
        conn.commit()

        fetch_stmt = text("""
                SELECT contents_id FROM contents 
                WHERE user_id = :user_id 
                ORDER BY update_date DESC 
                LIMIT 1
            """)
        result = conn.execute(fetch_stmt, {"user_id": user_id}).fetchone()
    print("[저장 완료] 추천 주제 2개 JSON 배열로 저장 완료")

    return result[0] if result else None
