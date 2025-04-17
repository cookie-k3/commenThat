import { useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useEffect, useState, useRef } from "react";
import LeftHeader from "../components/LeftHeader";
import TopBar from "../components/TopBar"; // 추가
import "../components/Home.css";
import { addMonths, isAfter } from "date-fns";
import { Line } from "react-chartjs-2";
import axios from "axios";

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

const Home = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAuth();
  const [checkedLogin, setCheckedLogin] = useState(false);
  const hasPromptedRef = useRef(false);

  const [subscribers, setSubscribers] = useState([]);
  const [views, setViews] = useState([]);

  const isFromLogout = location.state?.fromLogout;

  useEffect(() => {
    if (isFromLogout) return;
    if (!user && !hasPromptedRef.current) {
      hasPromptedRef.current = true;
      const goLogin = window.confirm(
        "로그인이 필요한 서비스입니다. 로그인 하시겠습니까?"
      );
      if (goLogin) navigate("/login");
      else navigate("/");
    } else if (user && !checkedLogin) {
      setCheckedLogin(true);
    }
  }, [user, checkedLogin, navigate, isFromLogout]);

  // 구독자 수 데이터
  useEffect(() => {
    const fetchData = async () => {
      try {
        const res = await axios.get(
          `http://localhost:8080/api/analysis/subscriber?userId=${user.userId}`
        );
        setSubscribers(res.data);
      } catch (err) {
        console.error("구독자 수 로딩 실패:", err);
      }
    };
    if (user?.userId) fetchData();
  }, [user]);

  // 조회수 데이터
  useEffect(() => {
    const fetchViews = async () => {
      try {
        const res = await axios.get(
          `http://localhost:8080/api/analysis/views?userId=${user.userId}`
        );
        setViews(res.data);
      } catch (err) {
        console.error("조회수 로딩 실패:", err);
      }
    };
    if (user?.userId) fetchViews();
  }, [user]);

  // 공통: 차트 옵션
  const miniChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
    },
    elements: {
      point: { radius: 0 },
    },
  };

  const subscriberChartData = {
    labels: subscribers.map((d) => d.date.slice(5, 10)),
    datasets: [
      {
        label: "",
        data: subscribers.map((d) => d.subscriber),
        borderColor: "rgba(75,192,192,1)",
        backgroundColor: "rgba(75,192,192,0.1)",
        fill: true,
        tension: 0.3,
      },
    ],
  };

  const viewsChartData = {
    labels: views.map((d) => d.date.slice(5, 10)),
    datasets: [
      {
        label: "",
        data: views.map((d) => d.totalViews),
        borderColor: "rgba(255, 99, 132, 1)",
        backgroundColor: "rgba(255, 99, 132, 0.1)",
        fill: true,
        tension: 0.3,
      },
    ],
  };

  if (!user || !checkedLogin) return null;

  return (
    <div className="home-container">
      <LeftHeader />
      <div className="home-main">
        {/* 상단 바 */}
        <TopBar />

        {/* 콘텐츠 카드 영역 */}
        <div className="dashboard">
          <div className="row">
            <div
              className="card clickable"
              onClick={() => navigate("/subscribers")}
            >
              <h3>구독자 수 분석</h3>
              {subscribers.length === 0 ? (
                <p style={{ color: "#999", fontSize: "14px" }}>데이터 없음</p>
              ) : (
                <div className="chart-wrapper">
                  <Line
                    data={subscriberChartData}
                    options={miniChartOptions}
                    height={200}
                  />
                </div>
              )}
            </div>

            <div className="card clickable" onClick={() => navigate("/views")}>
              <h3>조회수 분석</h3>
              {views.length === 0 ? (
                <p style={{ color: "#999", fontSize: "14px" }}>데이터 없음</p>
              ) : (
                <div className="chart-wrapper">
                  <Line
                    data={viewsChartData}
                    options={miniChartOptions}
                    height={200}
                  />
                </div>
              )}
            </div>
          </div>
          <div className="row">
            <div
              className="card clickable"
              onClick={() => navigate("/category")}
            >
              댓글 분석
            </div>
            <div
              className="card clickable"
              onClick={() => navigate("/recommend")}
            >
              콘텐츠 추천
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Home;
