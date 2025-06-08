import unittest
from unittest.mock import patch, MagicMock

class TestContentStatistics(unittest.TestCase):

    @patch("sqlalchemy.create_engine")
    def test_save_contents_statistics_db_write(self, mock_create_engine):
        # mock 객체 생성
        mock_engine = MagicMock()
        mock_conn = MagicMock()

        # engine.connect() → conn mock 연결
        mock_create_engine.return_value = mock_engine
        mock_engine.connect.return_value.__enter__.return_value = mock_conn

        # 함수 실행
        from contents.contents_analyze import save_contents_statistics
        save_contents_statistics(user_id=2)

        self.assertTrue(mock_conn.execute.called, "conn.execute()가 호출되지 않았습니다.")
        self.assertTrue(mock_conn.commit.called, "conn.commit()이 호출되지 않았습니다.")

if __name__ == "__main__":
    unittest.main(argv=["first-arg-is-ignored"], exit=False)