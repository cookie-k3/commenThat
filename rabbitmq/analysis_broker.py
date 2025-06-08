import os
import pika
import json
import threading
from dotenv import load_dotenv

load_dotenv()

RABBITMQ_HOST = os.getenv("RABBITMQ_HOST")
RABBITMQ_USER = os.getenv("RABBITMQ_USER")
RABBITMQ_PASSWORD = os.getenv("RABBITMQ_PASSWORD")

def publish_comment(video_id):
    try:
        credentials = pika.PlainCredentials(RABBITMQ_USER, RABBITMQ_PASSWORD)
        connection = pika.BlockingConnection(
            pika.ConnectionParameters(host=RABBITMQ_HOST, credentials=credentials)
        )
        channel = connection.channel()
        channel.queue_declare(queue='commentSuc', durable=True)

        message = {"videoId": video_id}
        channel.basic_publish(
            exchange='',
            routing_key='commentSuc',
            body=json.dumps(message)
        )
        print(f"[PUB] 완료 메시지 전송: {message}")
        connection.close()
    except Exception as e:
        print(f"[PUB ERROR] 전송 실패: {e}")

#수신
def subscriber_comment(process_task_callback):
    try:
        credentials = pika.PlainCredentials(RABBITMQ_USER, RABBITMQ_PASSWORD)
        connection = pika.BlockingConnection(
            pika.ConnectionParameters(host=RABBITMQ_HOST, credentials=credentials)
        )
        channel = connection.channel()
        channel.queue_declare(queue='commentReq', durable=True)

        def callback(ch, method, properties, body):
            try:
                message = json.loads(body)
                video_id = message.get("videoId")
                print(f"[SUB] 수신: video_id={video_id}")
                threading.Thread(
                    target=process_task_callback,
                    args=(video_id,),
                    daemon=True
                ).start()
                # 메시지 확인 (ack)
                ch.basic_ack(delivery_tag=method.delivery_tag)
            except Exception as e:
                print(f"[SUB ERROR] 메시지 처리 실패: {e}")
                print(f"[SUB ERROR] 수신한 원본: {body}")

        channel.basic_consume(queue='commentReq', on_message_callback=callback, auto_ack=False)
        print("[SUB] 대기 중... (CTRL+C 종료)")
        channel.start_consuming()
    except Exception as e:
        print(f"[SUB ERROR] 연결 실패: {e}")