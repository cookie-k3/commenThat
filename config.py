import os
from dotenv import load_dotenv
import pymysql

load_dotenv()

# SQLAlchemy에서 사용할 URL
DB_URL = os.getenv("DB_URL")  # 예: "mysql+pymysql://root:password@localhost:3306/team_project_db"

# pymysql 직접 연결용
def get_connection():
    return pymysql.connect(
        host=os.getenv("DB_HOST", "localhost"),
        port=int(os.getenv("DB_PORT", 3306)),
        user=os.getenv("DB_USER", "root"),
        password=os.getenv("DB_PASSWORD", ""),
        database=os.getenv("DB_NAME", "team_project_db"),
        charset="utf8mb4",
        cursorclass=pymysql.cursors.DictCursor
    )