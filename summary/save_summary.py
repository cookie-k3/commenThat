from db.insert_comment import category_map, get_comments_by_video_and_category
from config import get_connection

def save_summary_to_db(video_id, category_name, summary):
    category_id = category_map.get(category_name)
    if category_id is None:
        print(f"category_name '{category_name}'에 해당하는 category_id가 없습니다.")
        return

    comments = get_comments_by_video_and_category(video_id, category_name)
    comment_count = len(comments)

    connection = get_connection()
    try:
        with connection.cursor() as cursor:
            query = """
                INSERT INTO category_stat (video_id, category_id, count, summary)
                VALUES (%s, %s, %s, %s)
                ON DUPLICATE KEY UPDATE
                    count = VALUES(count),
                    summary = VALUES(summary)
            """
            cursor.execute(query, (video_id, category_id, comment_count, summary))
            connection.commit()
            print(f"DB에 저장 완료: video_id={video_id}, category={category_name}, count={comment_count}")
    except Exception as e:
        print(f"DB 저장 실패: {e}")
    finally:
        connection.close()