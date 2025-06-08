# 🧾 프로젝트 명명 규칙 (Naming Conventions)

본 문서는 View Service, Auto Processor, Analysis Service 각각의 명명 규칙을 정의합니다. 프로젝트 전체에서 일관된 코딩 스타일을 유지하기 위함입니다.

---

## 1. View Service 명명 규칙 (Spring Boot + React)

### 📌 Spring Boot (백엔드)

| 항목           | 명명 규칙                                       | 예시                              |
|----------------|--------------------------------------------------|-----------------------------------|
| 클래스명       | PascalCase                                       | `UserService`, `CommentController`|
| 인터페이스명   | 접두사 없이 명사형 사용                          | `ContentAnalyzer`                 |
| 메서드명       | camelCase                                        | `getSubscriberStats()`, `analyzeCategory()` |
| URL 경로       | 소문자 + 하이픈 사용                             | `/api/view-trend`                |
| Entity 필드명  | DB 컬럼명 snake_case → Java camelCase로 매핑     | `user_id` → `userId`             |

### 📌 React (프론트엔드)

| 항목             | 명명 규칙                                   | 예시                              |
|------------------|----------------------------------------------|-----------------------------------|
| 컴포넌트 파일명   | PascalCase                                  | `Subscriber.jsx`, `View.jsx`     |
| 함수/변수명       | camelCase                                   | `handleLogin`, `fetchVideoData`  |
| CSS 파일명        | 컴포넌트명과 동일하게 `.css` 확장자 사용   | `Home.css`                       |

---

## 2. Auto Processor 명명 규칙 (콘텐츠 추천 스케줄러 등)

| 항목        | 명명 규칙              | 예시                              |
|-------------|------------------------|-----------------------------------|
| 파일/모듈명 | 소문자 + 밑줄          | `contents_scheduler.py`           |
| 함수/변수명 | 소문자 + 밑줄          | `run_recommendation()`, `log_dir`|
| 상수        | 대문자 + 밑줄          | `DB_CONFIG`, `RESULT_QUEUE`       |

---

## 3. Analysis Service 명명 규칙 (RabbitMQ 분석 파이프라인)

| 항목        | 명명 규칙              | 예시                              |
|-------------|------------------------|-----------------------------------|
| 파일/모듈명 | 소문자 + 밑줄          | `rabbitmq_worker.py`              |
| 함수/변수명 | 소문자 + 밑줄          | `process_task()`, `category_map` |
| 상수        | 대문자 + 밑줄          | `RABBITMQ_HOST`, `CATEGORY_MAP`   |
| 폴더명      | 도메인 기준 소문자 사용 | `gpt/`, `sentiment/`, `db/`       |

---

## 공통 권장 사항

- 약어는 소문자로 작성 (`html_parser.py`), 단 클래스명에서는 PascalCase 허용 (`HtmlParser`)
- 모호한 축약은 피하고, 의미 있는 단어 사용 (`val`  → `value` )
- 핵심 키워드(`contents`, `category`, `summary` 등)는 모든 모듈에서 동일한 표기로 통일

---
