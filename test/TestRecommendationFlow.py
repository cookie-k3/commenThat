# PYTHONPATH=. python test/TestRecommendationFlow.py 실행
import unittest
from unittest.mock import patch, MagicMock

class TestContentRecommendationFlow(unittest.TestCase):

    @patch("builtins.open", new_callable=unittest.mock.mock_open)
    @patch("contents.contents_scheduler.content_recommendation")
    @patch("contents.contents_scheduler.save_contents_statistics")
    @patch("contents.contents_scheduler.publish_contents")
    @patch("contents.contents_scheduler.get_user_id2")
    @patch("sys.exit")
    def test_run_recommendation_success(
        self,
        mock_sys_exit,
        mock_get_user_id2,
        mock_publish,
        mock_save_stats,
        mock_recommend,
        mock_open
    ):
        mock_get_user_id2.return_value = [2]
        mock_recommend.return_value = 1001

        from contents.contents_scheduler import run_recommendation
        run_recommendation()

        mock_get_user_id2.assert_called_once()
        mock_save_stats.assert_called_once_with(2)
        mock_recommend.assert_called_once_with(2)
        mock_publish.assert_called_once_with(1001)
        mock_sys_exit.assert_not_called()

    @patch("contents.contents_scheduler.get_user_id2")
    @patch("sys.exit")
    def test_run_recommendation_no_users(self, mock_sys_exit, mock_get_user_id2):
        mock_get_user_id2.return_value = []

        from contents.contents_scheduler import run_recommendation
        run_recommendation()

        mock_get_user_id2.assert_called_once()
        mock_sys_exit.assert_not_called()


if __name__ == "__main__":
    unittest.main(argv=["first-arg-is-ignored"], exit=False)