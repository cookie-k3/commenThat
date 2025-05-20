import React, { useEffect, useState } from "react";
import axios from "axios";
import { useAuth } from "../context/AuthContext";
import LeftHeader from "../components/LeftHeader";
import TopBar from "../components/TopBar";
import "../components/ContentRecommend.css";
import { useNavigate } from "react-router-dom";

const categoryKeyMap = {
  joy: "즐거움",
  supportive: "응원",
  suggestion: "요청",
  hate: "혐오",
  question: "질문",
  praise: "칭찬",
  sympathy: "공감",
  congratulations: "축하",
  concern: "걱정",
  other: "기타",
};

const ContentRecommend = () => {
  const { user } = useAuth();
  const [summaryData, setSummaryData] = useState({});
  const navigate = useNavigate();

  useEffect(() => {
    const fetchData = async () => {
      try {
        const res = await axios.get(
          `/api/contents/summary?userId=${user.userId}`
        );
        setSummaryData(res.data);
        console.log("받은 데이터:", res.data); //데이터 확인용
      } catch (err) {
        console.error("데이터 요청 실패:", err);
      }
    };

    if (user?.userId) fetchData();
  }, [user]);

  const handleClick = (index) => {
    navigate(`/recommend/report/${index}`);
  };

  // JSON 파싱 (예외 처리 포함)
  const parseJson = (jsonString) => {
    try {
      return JSON.parse(jsonString);
    } catch {
      return null;
    }
  };

  const topView = parseJson(summaryData.topViewVideo);
  const topPositive = parseJson(summaryData.topPositiveVideo);
  const topNegative = parseJson(summaryData.topNegativeVideo);
  const topPositiveKeywords = parseJson(summaryData.topPositiveKeywords) || [];
  const topCategories = parseJson(summaryData.topCategories) || [];
  const recommendTopics = parseJson(summaryData.topic) || [];

  return (
    <div className="home-container">
      <LeftHeader />
      <div className="home-main">
        <TopBar />
        <h2>콘텐츠 추천</h2>

        <div className="dashboard">
          {/* 최고 조회수 영상 */}
          <h3>한 달간 최고 조회수 영상</h3>
          {topView && (
            <div className="view-top-video-card">
              <img
                src={topView.thumbnail}
                alt="썸네일"
                className="thumbnail-image"
              />
              <div className="video-info">
                <h4 className="video-title">{topView.title}</h4>
                <p className="video-views">
                  조회수: {topView.views?.toLocaleString()}회
                </p>
              </div>
            </div>
          )}

          {/* 긍정 댓글 비율 최고 영상 */}
          <h3>긍정 댓글 비율 최고 영상</h3>
          {topPositive && (
            <div className="view-top-video-card">
              <img
                src={topPositive.thumbnail}
                alt="썸네일"
                className="thumbnail-image"
              />
              <div className="video-info">
                <h4 className="video-title">{topPositive.title}</h4>
                <p className="video-views">
                  긍정 비율: {Math.round(topPositive.ratio * 100)}%
                </p>
              </div>
            </div>
          )}

          {/* 부정 댓글 비율 최고 영상 */}
          <h3>부정 댓글 비율 최고 영상</h3>
          {topNegative && (
            <div className="view-top-video-card">
              <img
                src={topNegative.thumbnail}
                alt="썸네일"
                className="thumbnail-image"
              />
              <div className="video-info">
                <h4 className="video-title">{topNegative.title}</h4>
                <p className="video-views">
                  부정 비율: {Math.round(topNegative.ratio * 100)}%
                </p>
              </div>
            </div>
          )}

          {/* 키워드 및 범주 순위 */}
          <div className="summary-section">
            <div className="summary-left">
              <h3>긍정 키워드 TOP 5</h3>
              <ol className="keyword-list">
                {topPositiveKeywords.map((kw, idx) => {
                  const emojis = ["🏆", "🥈", "🥉"];
                  const emoji = emojis[idx] || `${idx + 1}.`; // 1~3위는 이모지, 그 외에는 숫자
                  return (
                    <li key={idx} data-rank={idx + 1}>
                      <span className="rank-emoji">{emoji}</span> {kw}
                    </li>
                  );
                })}
              </ol>
            </div>

            <div className="summary-right">
              <h3>범주 TOP 5</h3>
              <ol className="category-list">
                {topCategories.map((cat, idx) => {
                  const emojis = ["🏆", "🥈", "🥉"];
                  const emoji = emojis[idx] || `${idx + 1}.`;
                  const koreanCategory = categoryKeyMap[cat] || cat; // 없을 경우 원본 유지
                  return (
                    <li key={idx} data-rank={idx + 1}>
                      <span className="rank-emoji">{emoji}</span>{" "}
                      {koreanCategory}
                    </li>
                  );
                })}
              </ol>
            </div>
          </div>

          {/* 콘텐츠 추천 키워드 (최대 2개 표시, 없으면 "키워드 없음" 버튼 표시) */}
          <h3>콘텐츠 추천 키워드</h3>
          <div className="recommend-keywords-grid">
            {(() => {
              let topics = [];

              try {
                if (
                  typeof recommendTopics === "string" &&
                  recommendTopics.trim() !== ""
                ) {
                  topics = JSON.parse(recommendTopics);
                } else if (Array.isArray(recommendTopics)) {
                  topics = recommendTopics;
                }
              } catch (e) {
                console.warn("추천 키워드 JSON 파싱 실패", e);
              }

              return (
                topics.length > 0 ? topics.slice(0, 2) : ["키워드 없음"]
              ).map((item, idx) => (
                <button
                  key={idx}
                  className={`keyword-button ${
                    item === "키워드 없음" ? "disabled" : ""
                  }`}
                  title={typeof item === "object" ? item.topic : item}
                  onClick={() => item !== "키워드 없음" && handleClick(idx)} // 인덱스를 넘김
                  disabled={item === "키워드 없음"}
                >
                  {typeof item === "object" ? item.topic : item}
                </button>
              ));
            })()}
          </div>
        </div>
      </div>
    </div>
  );
};

export default ContentRecommend;
