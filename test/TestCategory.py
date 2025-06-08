import unittest
from unittest.mock import patch, MagicMock
from rabbitmq.rabbitmq_worker import process_task


class ProcessTaskTest(unittest.TestCase):

    @patch("rabbitmq.rabbitmq_worker.categorize_batch")
    @patch("rabbitmq.rabbitmq_worker.group_by_token_limit")
    @patch("rabbitmq.rabbitmq_worker.predict_sentiment")
    @patch("rabbitmq.rabbitmq_worker.test_db_connection")
    @patch("rabbitmq.rabbitmq_worker.publish_comment")
    @patch("rabbitmq.rabbitmq_worker.save_summary_to_db")
    @patch("rabbitmq.rabbitmq_worker.summarize_category")
    @patch("rabbitmq.rabbitmq_worker.insert_category")
    @patch("rabbitmq.rabbitmq_worker.insert_sentiment")
    @patch("rabbitmq.rabbitmq_worker.get_recent_comments")
    @patch("rabbitmq.rabbitmq_worker.get_categories_for_video")
    def test_process_task_success(
        self,
        mock_get_categories,
        mock_get_comments,
        mock_insert_sentiment,
        mock_insert_category,
        mock_summarize,
        mock_save_summary,
        mock_publish,
        mock_test_conn,
        mock_sentiment,
        mock_group,
        mock_categorize
    ):
        # 준비: 댓글 데이터프레임 설정
        import pandas as pd
        df = pd.DataFrame({
            "comment": ["좋아요", "별로예요", "재밌어요"]
        })
        mock_get_comments.return_value = df

        # 감정 분석 결과 Mock
        mock_sentiment.return_value = ["positive", "negative", "positive"]

        # 토큰 분할과 GPT 범주화 결과 Mock
        mock_group.return_value = [["좋아요", "별로예요", "재밌어요"]]
        mock_categorize.return_value = ["joy", "hate", "joy"]

        # 요약 관련 Mock
        mock_get_categories.return_value = [1, 4]
        mock_summarize.return_value = "요약된 내용"

        # 실행
        process_task(video_id=123)

        # 검증
        mock_get_comments.assert_called_once()
        mock_sentiment.assert_called_once()
        mock_insert_sentiment.assert_called_once()
        mock_group.assert_called_once()
        mock_categorize.assert_called_once()
        mock_insert_category.assert_called_once()
        mock_summarize.assert_any_call(123, "joy")
        mock_save_summary.assert_any_call(123, "joy", "요약된 내용")
        mock_publish.assert_called_once_with(123)


if __name__ == "__main__":
    unittest.main()