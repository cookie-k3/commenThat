from openai import OpenAI
import os
from dotenv import load_dotenv
from gpt.prompt import get_summary_prompt
from db.insert_comment import get_comments_by_video_and_category
from gpt.token_utils import count_tokens

load_dotenv()
client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

MAX_TOKENS = 8192
RESERVED_FOR_RESPONSE = 600
MAX_COMMENTS = 80

def summarize_category(video_id, category_name):
    comments = get_comments_by_video_and_category(video_id, category_name)

    if not comments:
        print(f"요약 생략: '{category_name}' 범주에 댓글이 없습니다.")
        return "댓글이 없습니다."

    system_prompt = "당신은 유튜브 댓글을 분석하는 요약 전문가입니다."
    base_prompt = get_summary_prompt([], category_name)
    base_tokens = count_tokens(system_prompt) + count_tokens(base_prompt)

    selected_comments = []
    total_tokens = base_tokens

    for comment in comments:
        if comment is None:
            continue
        token_count = count_tokens(comment)
        if total_tokens + token_count > (MAX_TOKENS - RESERVED_FOR_RESPONSE):
            break
        if len(selected_comments) >= MAX_COMMENTS:
            break
        selected_comments.append(comment)
        total_tokens += token_count

    prompt = get_summary_prompt(selected_comments, category_name)

    try:
        print(f"GPT 요약 요청 시작 (category: {category_name}, 댓글 수: {len(selected_comments)}, 토큰 수: {total_tokens})")
        response = client.chat.completions.create(
            model="gpt-4o-mini",
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": prompt}
            ],
            temperature=0.3,
            max_tokens=1000
        )
        result = response.choices[0].message.content.strip()
        print(f"GPT 요약 완료 (category: {category_name})")
        return result
    except Exception as e:
        print(f"GPT 요약 실패 (category: {category_name}) - {e}")
        return "요약 실패"
