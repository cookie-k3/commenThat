import React, { useEffect, useState } from "react";
import axios from "axios";
import { Line } from "react-chartjs-2";
import { useAuth } from "../context/AuthContext";
import LeftHeader from "../components/LeftHeader";
import TopBar from "../components/TopBar";
import "../components/ContentRecommend.css";

import {
  Chart as ChartJS,
  LineElement,
  CategoryScale,
  LinearScale,
  PointElement,
  Tooltip,
  Legend,
  Filler,
} from "chart.js";

ChartJS.register(
  LineElement,
  CategoryScale,
  LinearScale,
  PointElement,
  Tooltip,
  Legend,
  Filler
);

const Subscriber = () => {
  const { user } = useAuth();
  const [data, setData] = useState([]);

  useEffect(() => {
    const fetchSubscriberData = async () => {
      try {
        const response = await axios.get(
          `http://localhost:8080/api/contents/topic-init?userId=${user.userId}`
        );
        console.log("✅ 받아온 주제 추천 데이터:", response.data); // 🔥 로그 추가
        setData(response.data); // 예: ["조개 먹방", "겨울 옷 하울", ...]
      } catch (error) {
        console.error("❌ 데이터 불러오기 실패:", error);
      }
    };

    if (user?.userId) fetchSubscriberData();
  }, [user]);

  return (
    <div className="home-container">
      <LeftHeader />
      <div className="home-main">
        <TopBar />
        <div className="subscriber-container">
          <h2>콘텐츠 추천</h2>
          {data.length === 0 ? (
            <p style={{ color: "#999", fontSize: "14px" }}>데이터 없음</p>
          ) : (
            <ul className="category-rank">
              {data.map((topic, idx) => (
                <li key={idx} className="rank-item">
                  <div className="rank-left">
                    <span className="rank-circle">{idx + 1}</span>
                    <span className="rank-label">{topic}</span>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </div>
  );
};

export default Subscriber;
