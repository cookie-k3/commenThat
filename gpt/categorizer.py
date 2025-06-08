import os
from openai import OpenAI
from dotenv import load_dotenv
from typing import List
from gpt.prompt import build_prompt
from gpt.token_utils import count_tokens

load_dotenv()
client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

MAX_TOKENS_PER_BATCH = 10000

def group_by_token_limit(comments: List[str], max_tokens=MAX_TOKENS_PER_BATCH,
                         model="gpt-4o") -> (List)[List[str]]:
    batches = []
    current_batch = []
    current_tokens = 0

    for comment in comments:
        tokens = count_tokens(comment, model) + 10
        if current_tokens + tokens > max_tokens:
            batches.append(current_batch)
            current_batch = [comment]
            current_tokens = tokens
        else:
            current_batch.append(comment)
            current_tokens += tokens

    if current_batch:
        batches.append(current_batch)
    return batches


def categorize_batch(comments: List[str]) -> List[str]:
    prompt = build_prompt(comments)

    try:
        response = client.chat.completions.create(
            model="gpt-4-turbo",  # 또는 "gpt-3.5-turbo"
            messages=[
                {
                    "role": "system",
                    "content": "You are a strict text classifier. Your task is to classify each input text into exactly one of the following 9 labels:\n\n"
                               "joy, supportive, suggestion, hate, question, praise, sympathy, congratulations, concern.\n\n"
                },
                {
                    "role": "user",
                    "content": prompt
                }
            ],
            max_tokens=1000,
            temperature=0.3,
            stop=["\n\n"]  # GPT가 중간에 불필요한 추가 설명 붙이는 걸 방지
        )
        output = response.choices[0].message.content.strip()
        lines = [line.strip().lower() for line in output.splitlines() if line.strip()]
        return lines

    except Exception as e:
        print("GPT 호출 실패:", e)
        return ["error"] * len(comments)