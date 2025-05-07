import React, { useEffect, useState } from "react";
import axios from "axios";
import "../components/Reputation.css";
import Header from "../components/Header";

const Reputation = () => {
  const [viewsRanking, setViewsRanking] = useState([]);
  const [subscribersRanking, setSubscribersRanking] = useState([]);
  const [positiveRanking, setPositiveRanking] = useState([]);

  useEffect(() => {
    fetchRankings();
  }, []);

  // 서버로부터 랭킹 데이터 불러오기
  const fetchRankings = async () => {
    try {
      const [viewsRes, subsRes, positiveRes] = await Promise.all([
        axios.get("/api/reputation/views"),
        axios.get("/api/reputation/subscribers"),
        axios.get("/api/reputation/positive"),
      ]);

      setViewsRanking(viewsRes.data);
      setSubscribersRanking(subsRes.data);
      setPositiveRanking(positiveRes.data);
    } catch (error) {
      console.error("랭킹 불러오기 실패:", error);
    }
  };

  // 종합 점수 기반 이달의 채널 계산
  const calculateChannelOfMonth = () => {
    const scoreMap = new Map(); // 사용자별 점수를 저장할 Map (userId -> { score, user })

    // 주어진 랭킹 데이터에서 상위 3명에게 점수를 부여하는 함수
    const applyScore = (ranking) => {
      if (!Array.isArray(ranking)) return; // 배열이 아니면 무시

      ranking.slice(0, 3).forEach((user, index) => {
        // 상위 3명만 추출하여 점수 계산
        const prev = scoreMap.get(user.userId) || { score: 0, user }; // 기존 점수 또는 초기값
        const addedScore = 3 - index; // 1등: 3점, 2등: 2점, 3등: 1점
        scoreMap.set(user.userId, { ...prev, score: prev.score + addedScore }); // 누적 점수 갱신
      });
    };

    // 조회수, 구독자 수, 긍정 댓글 수 각각에 대해 점수 적용
    applyScore(viewsRanking);
    applyScore(subscribersRanking);
    applyScore(positiveRanking);

    // 점수 기준으로 내림차순 정렬 후 가장 높은 사용자 반환
    const sorted = [...scoreMap.values()].sort((a, b) => b.score - a.score);
    return sorted.length > 0 ? sorted[0].user : null; // 1등 유저 반환 (없으면 null)
  };

  const channelOfMonth = calculateChannelOfMonth();

  // 특정 랭킹 데이터 렌더링
  const renderRanking = (data) => {
    if (!Array.isArray(data) || data.length === 0) {
      return <div>랭킹 정보가 없습니다.</div>;
    }

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
          <div className="ranking-top-name">{topUser.channelName}</div>
          <div className="user-total">{topUser.total?.toLocaleString()}</div>
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
              <div className="user-info">
                <div className="user-name">{user.channelName}</div>
                <div className="user-total">{user.total?.toLocaleString()}</div>
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
        {/* 이달의 채널 */}
        <div className="channel-of-month-section">
          <div className="channel-title">이달의 채널</div>
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

        {/* 각 항목별 킹왕짱 */}
        <div className="ranking-section">
          <div className="ranking-card">
            <h3>조회수 킹왕짱</h3>
            {renderRanking(viewsRanking)}
          </div>
          <div className="ranking-card">
            <h3>구독자수 킹왕짱</h3>
            {renderRanking(subscribersRanking)}
          </div>
          <div className="ranking-card">
            <h3>긍정 킹왕짱</h3>
            {renderRanking(positiveRanking)}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Reputation;
