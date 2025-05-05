import React, { useEffect, useState } from "react";
import axios from "axios";
import { useAuth } from "../context/AuthContext";
import LeftHeader from "../components/LeftHeader";
import TopBar from "../components/TopBar";
import "../components/ContentRecommend.css";
import { useNavigate } from "react-router-dom"; // 페이지 이동 기능 추가

const ContentRecommend = () => {
  const { user } = useAuth();
  const [topicData, setTopicData] = useState([]); // 주제와 URL 리스트 데이터를 저장
  const navigate = useNavigate(); // 페이지 이동하기 위한(추천 보고서로 이동)

  useEffect(() => {
    const fetchData = async () => {
      try {
        // 백엔드에서 사용자 ID로 콘텐츠 추천 데이터 받아오기
        const res = await axios.get(
          `/api/contents/topic-urls?userId=${user.userId}`
        );
        console.log("받아온 데이터:", res.data);
        setTopicData(res.data); // 예: ["조개 먹방", "겨울 옷 하울", ...]
      } catch (err) {
        console.error("데이터 요청 실패:", err);
      }
    };

    if (user?.userId) fetchData(); // 유저 ID가 존재할 때만 fetch 실행
  }, [user]);

  // 추천 보고서 페이지로 이동하는 함수 추가
  const handleClick = (contentsId) => {
    console.log("클릭됨", contentsId);
    navigate(`/recommend/report/${contentsId}`);
  };

  return (
    <div className="home-container">
      <LeftHeader />
      <div className="home-main">
        <TopBar />
        <h2>콘텐츠 추천</h2>

        {/* 키워드+URL 세트를 수직 정렬 후 가로로 나열 */}
        <div className="card-wrapper">
          {topicData.map((item, idx) => (
            <div key={idx} className="topic-url-group">
              {/* 키워드 카드 */}
              <div
                className="topic-only-card"
                title={item.topic}
                onClick={() => handleClick(item.contentsId)} // 키워드 클릭 시 보고서로 이동
                style={{ cursor: "pointer" }}
              >
                {item.topic}
              </div>

              {/* 관련 URL 카드 */}
              <div className="url-list-card">
                <ul className="video-url-list">
                  {item.referenceDtos.map((ref, i) => {
                    const cleanUrl = ref.url.replace(/"/g, ""); // url 큰따옴표 제거 처리
                    return (
                      <li key={i} className="video-url-item">
                        <span className="rank-circle">{i + 1}</span>
                        <a
                          href={cleanUrl}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="video-url-link"
                        >
                          {/* 새롭게 title과 views 표시 */}
                          <div className="video-url-title">{ref.title}</div>
                          <div className="video-url-views">
                            {ref.views} views
                          </div>
                        </a>
                      </li>
                    );
                  })}
                </ul>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default ContentRecommend;
