import React, { useState } from "react";
import axios from "axios";
import { Line } from "react-chartjs-2";
import { useAuth } from "../context/AuthContext";
import LeftHeader from "../components/LeftHeader";
import VideoSelectTopBar from "../components/VideoSelectTopBar";
import "../components/ViewByVideo.css";

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

const ViewByVideo = () => {
  const { user } = useAuth();
  const [videoId, setVideoId] = useState(null);
  const [viewData, setViewData] = useState([]);

  const handleVideoSelect = async (selectedId) => {
    setVideoId(selectedId);

    try {
      const res = await axios.get(
        `http://localhost:8080/api/analysis/video-views?videoId=${selectedId}`
      );
      const raw = res.data;

      console.log("[서버 응답] 원본 raw 데이터:", raw);

      // 날짜 오름차순 정렬
      const sorted = [...raw].sort(
        (a, b) => new Date(a.date) - new Date(b.date)
      );
      console.log("[정렬된 데이터]", sorted);

      setViewData(sorted);
    } catch (err) {
      console.error("영상별 조회수 데이터 로딩 실패:", err);
    }
  };

  const chartData = {
    labels: viewData.map((d) => d.date),
    datasets: [
      {
        label: "누적 조회수",
        data: viewData.map((d) => d.views),
        borderColor: "rgba(75, 192, 192, 1)",
        backgroundColor: "rgba(75, 192, 192, 0.2)",
        fill: true,
        tension: 0.3,
      },
    ],
  };

  const options = {
    responsive: true,
    plugins: {
      legend: {
        position: "top",
      },
    },
  };

  return (
    <div className="home-container">
      <LeftHeader />
      <div className="home-main">
        {/* 영상 선택 상단 바 */}
        <VideoSelectTopBar
          fetchUrl="http://localhost:8080/api/analysis/view-chart-init"
          onVideoSelect={handleVideoSelect}
          initialVideoId={null}
        />

        <div className="subscriber-container">
          <h2>영상별 조회수 추이</h2>
          {viewData.length === 0 ? (
            <p style={{ color: "#999", fontSize: "14px" }}>데이터 없음</p>
          ) : (
            <Line data={chartData} options={options} />
          )}
        </div>
      </div>
    </div>
  );
};

export default ViewByVideo;
