import React, { useEffect, useState } from "react";
import axios from "axios";
import { useAuth } from "../context/AuthContext";
import LeftHeader from "../components/LeftHeader";
import TopBar from "../components/TopBar";
import "../components/ContentRecommend.css";
import { useNavigate } from "react-router-dom";

const ContentRecommend = () => {
  const { user } = useAuth();
  const [topicData, setTopicData] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchData = async () => {
      try {
        const res = await axios.get(
          `/api/contents/topic-urls?userId=${user.userId}`
        );
        setTopicData(res.data);
      } catch (err) {
        console.error("데이터 요청 실패:", err);
      }
    };

    if (user?.userId) fetchData();
  }, [user]);

  const handleClick = (contentsId) => {
    navigate(`/recommend/report/${contentsId}`);
  };

  // topicData를 2개씩 나눔
  const rows = [];
  for (let i = 0; i < topicData.length; i += 2) {
    rows.push(topicData.slice(i, i + 2));
  }

  return (
    <div className="home-container">
      <LeftHeader />
      <div className="home-main">
        <TopBar />
        <h2>콘텐츠 추천</h2>

        <div className="dashboard">
          {rows.map((row, rowIdx) => (
            <div key={rowIdx} className="row">
              {row.map((item, idx) => (
                <div key={idx} className="topic-card-wrapper">
                  {/*  키워드 버튼 */}
                  <div
                    className="topic-only-card"
                    title={item.topic}
                    onClick={() => handleClick(item.contentsId)}
                    style={{ cursor: "pointer" }}
                  >
                    {item.topic}
                  </div>

                  {/* 관련 영상 URL 목록 */}
                  <div className="url-list-card">
                    <div className="thumbnail-grid">
                      {item.referenceDtos
                        .sort((a, b) => b.views - a.views)
                        .slice(0, 4)
                        .map((ref, i) => {
                          const cleanUrl = ref.url.replace(/"/g, "");
                          return (
                            <div key={i} className="thumbnail-wrapper">
                              <a
                                href={cleanUrl} // 이동할 영상 URL
                                target="_blank" // 새 탭에서 열기
                                rel="noopener noreferrer"
                                className="thumbnail-link"
                              >
                                <img
                                  src={ref.img}
                                  alt={ref.title}
                                  className="thumbnail-img"
                                />
                                <div className="thumbnail-hover-title">
                                  {ref.title}
                                </div>
                              </a>
                            </div>
                          );
                        })}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default ContentRecommend;
