import os
import sys
import pymysql  # MySQL 사용 시 필요
import pika
import json
from datetime import datetime
from dotenv import load_dotenv
from contents.contents_generator import content_recommendation
from contents.contents_analyze import save_contents_statistics

# ====== 환경변수 로딩 ======
load_dotenv()
# ====== DB 설정 ======
DB_CONFIG = {
    "host": os.getenv("DB_HOST"),
    "user": os.getenv("DB_USER"),
    "password": os.getenv("DB_PASSWORD"),
    "database": os.getenv("DB_NAME"),
    "port": int(os.getenv("DB_PORT", 3307))
}

# ====== RabbitMQ 설정 ======
RABBITMQ_HOST = os.getenv("RABBITMQ_HOST")
RABBITMQ_USER = os.getenv("RABBITMQ_USER")
RABBITMQ_PASSWORD = os.getenv("RABBITMQ_PASSWORD")

RESULT_QUEUE = "urlReq"

# ====== 로그 설정 ======
LOG_DIR = "./logs"
os.makedirs(LOG_DIR, exist_ok=True)
LOG_FILE = os.path.join(LOG_DIR, "recommendation_log.txt")

def log(message):
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    with open(LOG_FILE, "a", encoding="utf-8") as f:
        f.write(f"[{timestamp}] {message}\n")

def publish_contents(contents_id):
    try:
        credentials = pika.PlainCredentials(RABBITMQ_USER, RABBITMQ_PASSWORD)
        connection = pika.BlockingConnection(
            pika.ConnectionParameters(
                host=RABBITMQ_HOST,
                credentials=credentials
            )
        )

        channel = connection.channel()
        channel.queue_declare(queue=RESULT_QUEUE,durable=True)

        message = {"contentsId": contents_id}
        channel.basic_publish(
            exchange='',
            routing_key=RESULT_QUEUE,
            body=json.dumps(message)
        )
        log(f"[PUB] 완료 메시지 전송: {message}")
        connection.close()
    except Exception as e:
        log(f"[ERROR] RabbitMQ 전송 실패: {e}")

def get_all_user_ids():
    try:
        conn = pymysql.connect(**DB_CONFIG)
        cursor = conn.cursor()
        cursor.execute("SELECT DISTINCT user_id FROM user")
        user_ids = [row[0] for row in cursor.fetchall()]
        log(f"[INFO] 가져온 유저 ID 목록: {user_ids}")
        cursor.close()
        conn.close()
        return user_ids
    except Exception as e:
        log(f"[ERROR] 유저 ID 조회 실패: {e}")
        sys.exit(1)

def get_user_id2():
    print("[ACTUAL FUNCTION] get_user_id2() 실행됨")
    try:
        conn = pymysql.connect(**DB_CONFIG)
        cursor = conn.cursor()
        cursor.execute("SELECT DISTINCT user_id FROM user WHERE user_id = 2")
        user_ids = [row[0] for row in cursor.fetchall()]
        log(f"[INFO] 가져온 유저 ID 목록: {user_ids}")
        cursor.close()
        conn.close()
        return user_ids
    except Exception as e:
        log(f"[ERROR] 유저 ID 조회 실패: {e}")
        sys.exit(1)

def run_recommendation():
    try:
        # 나중에 변경 필요 지금은 테스트 용
        user_ids = get_user_id2()
        if not user_ids:
            log("추천 대상 유저 없음. 작업 종료.")
            return

        for user_id in user_ids:
            log(f"콘텐츠 추천 시작: user_id={user_id}")
            save_contents_statistics(user_id)
            contents_id = content_recommendation(user_id)
            log(f"콘텐츠 추천 완료: user_id={user_id}")

            # RabbitMQ 메세지 전송
            publish_contents(contents_id)

        log("모든 콘텐츠 추천 작업 완료.\n")

    except Exception as e:
        log(f"[ERROR] 콘텐츠 추천 중 오류 발생: {e}")
        sys.exit(1)

if __name__ == "__main__":
    run_recommendation()