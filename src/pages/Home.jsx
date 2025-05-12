import { useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useEffect, useState, useRef } from "react";
import LeftHeader from "../components/LeftHeader";
import TopBar from "../components/TopBar";
import "../components/Home.css";
import { Line } from "react-chartjs-2";
import axios from "axios";

import {
  Chart as ChartJS,
  LineElement,
  CategoryScale,
  LinearScale,
  PointElement,
  ArcElement,
  Tooltip as ChartTooltip,
  Legend as ChartLegend,
  Filler,
} from "chart.js";

import { PieChart, Pie as RePie, Cell, Tooltip as ReTooltip } from "recharts";

ChartJS.register(
  LineElement,
  CategoryScale,
  LinearScale,
  PointElement,
  ArcElement,
  ChartTooltip,
  ChartLegend,
  Filler
);

const Home = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuth();
  const [checkedLogin, setCheckedLogin] = useState(false);
  const hasPromptedRef = useRef(false);

  const [views, setViews] = useState([]);
  const [categoryData, setCategoryData] = useState([]);
  const [sentimentData, setSentimentData] = useState([]);
  const [topics, setTopics] = useState([]);
  const [fromLogout, setFromLogout] = useState(false);
  const [videoTrendData, setVideoTrendData] = useState([]);

  const [summary, setSummary] = useState(null);

  useEffect(() => {
    const isLogout = localStorage.getItem("fromLogout");
    if (isLogout === "true") {
      setFromLogout(true);
      localStorage.removeItem("fromLogout");
    }
  }, []);

  useEffect(() => {
    if (fromLogout) return;
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
  }, [user, checkedLogin, navigate, fromLogout]);

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
  useEffect(() => {
    const fetchVideoTrend = async () => {
      try {
        // 1. 최근 영상 ID 조회
        const res1 = await axios.get(
          `http://localhost:8080/api/comments/senti-chart-init?userId=${user.userId}`
        );
        const recentVideoId = res1.data.videoId;

        if (!recentVideoId) return;

        // 2. 해당 영상에 대한 조회수 추이 조회
        const res2 = await axios.get(
          `http://localhost:8080/api/analysis/video-views?videoId=${recentVideoId}`
        );

        const sorted = [...res2.data].sort(
          (a, b) => new Date(a.date) - new Date(b.date)
        );
        setVideoTrendData(sorted.slice(-7)); // 최근 7일치만
      } catch (e) {
        console.error("영상별 조회수 미리보기 로딩 실패:", e);
      }
    };

    if (user?.userId) fetchVideoTrend();
  }, [user]);

  useEffect(() => {
    const fetchCategory = async () => {
      try {
        const res = await axios.get(
          `http://localhost:8080/api/comments/category-chart-init?userId=${user.userId}`
        );
        const stat = res.data.data.statCountDto;
        if (!stat) {
          setCategoryData([]);
          return;
        }
        const parsed = Object.entries(stat).map(([key, value]) => ({
          name: key,
          value: Number(value),
        }));
        setCategoryData(parsed);
      } catch (err) {
        console.error("범주 데이터 로딩 실패:", err);
      }
    };
    if (user?.userId) fetchCategory();
  }, [user]);

  useEffect(() => {
    const fetchSentiment = async () => {
      try {
        const res = await axios.get(
          `http://localhost:8080/api/comments/senti-chart-init?userId=${user.userId}`
        );
        const data = res.data;
        setSentimentData([
          { name: "부정", value: data.negativeCount },
          { name: "긍정", value: data.positiveCount },
        ]);
      } catch (err) {
        console.error("긍부정 데이터 로딩 실패:", err);
      }
    };
    if (user?.userId) fetchSentiment();
  }, [user]);

  useEffect(() => {
    const fetchTopics = async () => {
      try {
        const res = await axios.get(
          `http://localhost:8080/api/contents/topic-init?userId=${user.userId}`
        );
        setTopics(res.data);
      } catch (err) {
        console.error("콘텐츠 추천 데이터 로딩 실패:", err);
      }
    };
    if (user?.userId) fetchTopics();
  }, [user]);

  useEffect(() => {
    const fetchSummary = async () => {
      try {
        const res = await axios.get(
          `http://localhost:8080/api/contents/summary?userId=${user.userId}`
        );
        setSummary(res.data);
      } catch (err) {
        console.error("콘텐츠 요약 데이터 로딩 실패:", err);
      }
    };

    if (user?.userId) fetchSummary();
  }, [user]);

  useEffect(() => {
    if (user) {
      const timer = setTimeout(() => {
        logout();
        navigate("/login");
        alert("오랜 시간 활동이 없어 자동으로 로그아웃되었습니다.");
      }, 2 * 60 * 60 * 1000);

      return () => clearTimeout(timer);
    }
  }, [user]);

  if (!user && !fromLogout) return null;
  if (!user || !checkedLogin) return null;

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

  return (
    <div className="home-container">
      <LeftHeader />
      <div className="home-main">
        <TopBar />

        <div className="dashboard">
          <div className="row">
            {/* 채널 조회수 분석 카드 */}
            <div className="card clickable" onClick={() => navigate("/views")}>
              <h3>채널 조회수 분석</h3>
              {views.length === 0 ? (
                <p style={{ color: "#999", fontSize: "14px" }}>데이터 없음</p>
              ) : (
                <div className="chart-wrapper">
                  <Line
                    data={{
                      labels: views.slice(-7).map((d) => d.date.slice(5, 10)), // 마지막 7개만
                      datasets: [
                        {
                          label: "",
                          data: views.slice(-7).map((d) => d.totalViews),
                          borderColor: "rgba(255, 99, 132, 1)",
                          backgroundColor: "rgba(255, 99, 132, 0.1)",
                          fill: true,
                          tension: 0.3,
                        },
                      ],
                    }}
                    options={miniChartOptions}
                    height={200}
                  />
                </div>
              )}
            </div>

            {/* 영상별 조회수 분석 카드 */}
            <div
              className="card clickable"
              onClick={() => navigate("/views/by-video")}
            >
              <h3>영상별 조회수 분석</h3>
              {videoTrendData.length < 2 ? (
                <p style={{ color: "#999", fontSize: "14px" }}>데이터 없음</p>
              ) : (
                <div className="chart-wrapper">
                  <Line
                    data={{
                      labels: videoTrendData.map((d) => d.date.slice(5, 10)), // MM-DD
                      datasets: [
                        {
                          label: "",
                          data: videoTrendData.map((d) => d.views), // ✅ views 사용
                          borderColor: "rgba(75, 192, 192, 1)",
                          backgroundColor: "rgba(75, 192, 192, 0.2)",
                          fill: true,
                          tension: 0.3,
                        },
                      ],
                    }}
                    options={miniChartOptions}
                    height={200}
                  />
                </div>
              )}
            </div>
          </div>

          <div className="row">
            {/* 댓글 분석 카드 */}
            <div className="card" style={{ flex: 1 }}>
              <h3>댓글 분석</h3>
              <div
                style={{
                  display: "flex",
                  gap: "1rem",
                  marginTop: "1rem",
                  justifyContent: "space-between",
                  width: "100%",
                }}
              >
                <div
                  className="mini-chart clickable"
                  style={{ flex: 1, textAlign: "center" }}
                  onClick={() => navigate("/category")}
                >
                  <PieChart width={180} height={180}>
                    <RePie
                      data={categoryData
                        .filter((d) => d.name !== "other")
                        .map((d) => ({ name: d.name, value: d.value }))}
                      cx="50%"
                      cy="50%"
                      outerRadius={80}
                      dataKey="value"
                      labelLine={false}
                    >
                      {categoryData.map((_, i) => (
                        <Cell
                          key={`cat-${i}`}
                          fill={
                            [
                              "#F8C8C4",
                              "#AED9E0",
                              "#E6B8B7",
                              "#C1BBDD",
                              "#FFF2AC",
                              "#F6C1B4",
                              "#AED9E0",
                              "#B0C9E8",
                              "#F8C8C4",
                              "#B2E2C2",
                              "#F9D5C2",
                              "#BFD7EA",
                              "#A0A0A0",
                              "#E0E0E0",
                            ][i % 10]
                          }
                        />
                      ))}
                    </RePie>
                    <ReTooltip />
                  </PieChart>
                  <p style={{ fontSize: "14px", marginTop: "0.5rem" }}>
                    댓글 범주화
                  </p>
                </div>

                <div
                  className="mini-chart clickable"
                  style={{ flex: 1, textAlign: "center" }}
                  onClick={() => navigate("/sentiment")}
                >
                  {sentimentData.length === 0 ||
                  sentimentData.every((d) => d.value === 0) ? (
                    <div
                      style={{
                        height: "180px",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                      }}
                    >
                      <p style={{ color: "#999", fontSize: "14px" }}>
                        데이터 없음
                      </p>
                    </div>
                  ) : (
                    <PieChart width={180} height={180}>
                      <RePie
                        data={sentimentData}
                        cx="50%"
                        cy="50%"
                        innerRadius={50}
                        outerRadius={80}
                        dataKey="value"
                        labelLine={false}
                      >
                        {sentimentData.map((_, i) => (
                          <Cell
                            key={`senti-${i}`}
                            fill={["#A8CFF2", "#ECB4D6"][i % 2]}
                          />
                        ))}
                      </RePie>
                      <ReTooltip />
                    </PieChart>
                  )}
                  <p style={{ fontSize: "14px", marginTop: "0.5rem" }}>
                    긍부정 분석
                  </p>
                </div>
              </div>
            </div>

            {/* 콘텐츠 추천 카드 */}
            {/* 콘텐츠 추천 카드 */}
            <div
              className="card clickable"
              onClick={() => navigate("/recommend")}
            >
              <h3 style={{ marginBottom: "20px" }}>콘텐츠 추천</h3>

              {summary && summary.topViewVideo && (
                <div className="mini-summary-box">
                  <h4 style={{ marginBottom: "4px" }}>
                    한 달간 최고 조회수 영상
                  </h4>
                  <div className="mini-video-row">
                    <img
                      src={JSON.parse(summary.topViewVideo).thumbnail}
                      alt="썸네일"
                      className="mini-thumbnail"
                    />
                    <div>
                      <p className="mini-title">
                        {JSON.parse(summary.topViewVideo).title}
                      </p>
                      <p style={{ fontSize: "13px", color: "#555" }}>
                        조회수:{" "}
                        {Number(
                          JSON.parse(summary.topViewVideo).views
                        ).toLocaleString()}
                        회
                      </p>
                    </div>
                  </div>
                </div>
              )}

              <h4 className="recommend-label">추천 키워드</h4>
              {topics.length === 0 ? (
                <p style={{ color: "#999", fontSize: "14px" }}>데이터 없음</p>
              ) : (
                <div className="horizontal-topic-list">
                  {topics.slice(0, 4).map((topic, idx) => (
                    <div key={idx} className="horizontal-topic-item">
                      <span className="rank-circle">{idx + 1}</span>
                      <span className="rank-label">{topic}</span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Home;
