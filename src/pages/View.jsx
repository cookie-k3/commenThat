import React, { useEffect, useState } from "react";
import axios from "axios";
import { Line } from "react-chartjs-2";
import { useAuth } from "../context/AuthContext";
import LeftHeader from "../components/LeftHeader";
import TopBar from "../components/TopBar";
import "../components/View.css";

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

const View = () => {
  const { user } = useAuth();
  const [data, setData] = useState([]);

  useEffect(() => {
    const fetchViewData = async () => {
      try {
        const response = await axios.get(
          `http://localhost:8080/api/analysis/views?userId=${user.userId}`
        );
        setData(response.data);
      } catch (error) {
        console.error("조회수 데이터 불러오기 실패:", error);
      }
    };
    if (user?.userId) fetchViewData();
  }, [user]);
  const recentData = data.slice(-7);
  const chartData = {
    labels: recentData.map((entry) => entry.date.slice(0, 10)),
    datasets: [
      {
        label: "총 조회수",
        data: recentData.map((entry) => entry.totalViews),
        borderColor: "rgba(255, 99, 132, 1)",
        backgroundColor: "rgba(255, 99, 132, 0.2)",
        fill: true,
        tension: 0.2,
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
        <TopBar />
        <div className="subscriber-container">
          <h2>채널 조회수 추이</h2>
          {data.length === 0 ? (
            <p style={{ color: "#999", fontSize: "14px" }}>데이터 없음</p>
          ) : (
            <Line data={chartData} options={options} />
          )}
        </div>
      </div>
    </div>
  );
};

export default View;
