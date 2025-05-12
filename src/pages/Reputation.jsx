import React, { useEffect, useState } from "react";
import axios from "axios";
import "../components/Reputation.css";
import Header from "../components/Header";

const Reputation = () => {
  const [viewsRanking, setViewsRanking] = useState([]);
  const [uploadRanking, setUploadRanking] = useState([]);
  const [positiveRanking, setPositiveRanking] = useState([]);
  const [channelOfMonth, setChannelOfMonth] = useState(null); // ✅ 이달의 채널 상태 추가
  const [channelExplanation, setChannelExplanation] = useState(""); // ✅ 설명 메시지 상태 추가

  // 컴포넌트 마운트 시 랭킹 데이터 로딩
  useEffect(() => {
    fetchRankings();
  }, []);

  // 업로드 랭킹 콘솔 확인용
  useEffect(() => {
    console.log("📦 Upload Ranking 데이터 확인:");
    uploadRanking.forEach((user, i) => {
      console.log(
        `${i + 1}위 - userId: ${user.userId}, 채널명: ${
          user.channelName
        }, 업로드 수: ${user.total}`
      );
    });
  }, [uploadRanking]);

  // 모든 랭킹 API 요청
  const fetchRankings = async () => {
    try {
      const [viewsRes, uploadsRes, positiveRes] = await Promise.all([
        axios.get("/api/reputation/views"),
        axios.get("/api/reputation/uploads"),
        axios.get("/api/reputation/positive"),
      ]);
      setViewsRanking(viewsRes.data);
      setUploadRanking(uploadsRes.data);
      setPositiveRanking(positiveRes.data);
    } catch (error) {
      console.error("랭킹 불러오기 실패:", error);
    }
  };

  // 랭킹 데이터가 모두 로드된 뒤 이달의 채널 계산 실행
  useEffect(() => {
    if (
      viewsRanking.length > 0 &&
      uploadRanking.length > 0 &&
      positiveRanking.length > 0
    ) {
      const { winner, explanation } = calculateChannelOfMonth();
      setChannelOfMonth(winner);
      setChannelExplanation(explanation);
    }
  }, [viewsRanking, uploadRanking, positiveRanking]);

  // 이달의 채널 선정 로직
  const calculateChannelOfMonth = () => {
    const scoreMap = new Map();

    // 각 랭킹 항목별 점수 부여 함수
    const applyScore = (ranking, category) => {
      if (!Array.isArray(ranking)) return;

      ranking.slice(0, 3).forEach((user, index) => {
        // 점수: 1등 5점, 2등 3점, 3등 1점
        const addedScore = index === 0 ? 5 : index === 1 ? 3 : 1;

        // 기존 사용자 정보 불러오거나 초기값 세팅
        const prev = scoreMap.get(user.userId) || {
          score: 0,
          user,
          ranks: { views: null, uploads: null, positive: null },
        };

        // 점수 누적
        prev.score += addedScore;

        // 해당 카테고리의 순위 기록 (1등부터 시작)
        prev.ranks[category] = index + 1;

        // 다시 맵에 저장
        scoreMap.set(user.userId, prev);
      });
    };

    // 조회수, 업로드 수, 긍정 댓글 순위 반영
    applyScore(viewsRanking, "views");
    applyScore(uploadRanking, "uploads");
    applyScore(positiveRanking, "positive");

    // 총점 기준으로 내림차순 정렬, 동점자는 우선순위(긍정 > 조회수 > 성실)로 비교
    const sorted = [...scoreMap.values()].sort((a, b) => {
      if (b.score !== a.score) return b.score - a.score;

      // 동점 시 우선순위: 긍정 → 조회수 → 성실
      const aPositive = a.ranks.positive ?? 999;
      const bPositive = b.ranks.positive ?? 999;
      if (aPositive !== bPositive) return aPositive - bPositive;

      const aViews = a.ranks.views ?? 999;
      const bViews = b.ranks.views ?? 999;
      if (aViews !== bViews) return aViews - bViews;

      const aUploads = a.ranks.uploads ?? 999;
      const bUploads = b.ranks.uploads ?? 999;
      return aUploads - bUploads;
    });

    // 최고 점수 추출
    const topScore = sorted[0]?.score ?? 0;

    // 동점인 사용자 목록 추출
    const tied = sorted.filter((item) => item.score === topScore);

    // 현재 월 (ex. 5월)
    const month = new Date().getMonth() + 1;

    // 점수 계산 기준 메시지 `점수는 1등 5점, 2등 3점, 3등 1점으로 계산됩니다.`
    const scoreGuide = `점수는 시스템의 기준에 따라 계산됩니다.`;

    let explanation = "";

    if (tied.length > 1) {
      // 동점자 있을 경우 메시지
      const names = tied.map((i) => i.user.channelName).join(", ");
      explanation = `이달의 채널은 우선순위(긍정 > 조회수 > 성실)에 따라 ${sorted[0].user.channelName}이(가) 선택되었습니다.`;
    } else {
      // 동점자 없을 경우 메시지
      explanation = `이달의 채널은 ${sorted[0].user.channelName}이며, ${scoreGuide}`;
    }

    // 선택된 사용자와 설명 반환
    return { winner: sorted[0]?.user ?? null, explanation };
  };

  // 각 부문별 랭킹 렌더링
  const renderRanking = (data, type) => {
    if (!Array.isArray(data) || data.length === 0) {
      return <div>랭킹 정보가 없습니다.</div>;
    }

    const getUnit = (type) => {
      if (type === "views") return "%";
      if (type === "uploads") return "개";
      if (type === "positive") return "%";
      return "";
    };

    const unit = getUnit(type);
    const topUser = data[0];
    const rest = data.slice(1);

    return (
      <>
        <div className="ranking-top-user">
          <div className="ranking-crown">👑</div>
          <img
            src={topUser.channelImg}
            alt="top"
            className="ranking-top-img"
            onError={(e) => (e.target.src = "/default_profile.png")}
          />
          <div className="top-info">
            <div className="ranking-top-name">{topUser.channelName}</div>
            <div className="user-total">
              {topUser.total?.toLocaleString()}
              {unit}
            </div>
          </div>
        </div>
        <div className="ranking-list">
          {[topUser, ...rest].map((user, index) => (
            <div key={user.userId} className="ranking-item">
              <div className="rank-number">{index + 1}</div>
              <img
                src={user.channelImg}
                alt="profile"
                className="profile-img"
                onError={(e) => (e.target.src = "/default_profile.png")}
              />
              {/* 💡 이름과 수치를 좌우로 분리 정렬 */}
              <div className="user-info-flex">
                <div className="user-name">{user.channelName}</div>
                <div className="user-total-wrapper">
                  <span className="user-total">
                    {user.total?.toLocaleString()}
                  </span>
                  <span className="user-unit">{unit}</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      </>
    );
  };

  return (
    <div className="reputation-page">
      <Header />
      <div className="reputation-container">
        <div className="channel-of-month-section">
          <div className="channel-inner">
            <div className="channel-title">
              이달의 채널
              {channelExplanation && (
                <div className="tooltip-wrapper">
                  <span className="info-icon">ℹ️</span>
                  <div className="tooltip-box">{channelExplanation}</div>
                </div>
              )}
            </div>
            <div className="channel-divider"></div>
            {channelOfMonth ? (
              <div className="channel-details">
                <div className="crown-icon">👑</div>
                <img
                  src={channelOfMonth.channelImg}
                  alt="channel"
                  className="channel-img"
                  onError={(e) => (e.target.src = "/default_profile.png")}
                />
                <div className="channel-name">{channelOfMonth.channelName}</div>
              </div>
            ) : (
              <div>이달의 채널 정보가 없습니다.</div>
            )}
          </div>
        </div>

        <div className="ranking-section">
          <div className="ranking-card">
            <h3>조회수 킹왕짱</h3>
            {renderRanking(viewsRanking, "views")}
          </div>
          <div className="ranking-card">
            <h3>성실 킹왕짱</h3>
            {renderRanking(uploadRanking, "uploads")}
          </div>
          <div className="ranking-card">
            <h3>긍정 킹왕짱</h3>
            {renderRanking(positiveRanking, "positive")}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Reputation;
