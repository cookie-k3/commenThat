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

  const [subscribers, setSubscribers] = useState([]);
  const [views, setViews] = useState([]);
  const [categoryData, setCategoryData] = useState([]);
  const [sentimentData, setSentimentData] = useState([]);
  const [topics, setTopics] = useState([]); // 콘텐츠 추천용

  const [fromLogout, setFromLogout] = useState(false); // 로그아웃 후 이동한 경우를 구분하기 위한 상태 (초기값은 false)

  // localStorage로 logout flag 감지
  useEffect(() => {
    const isLogout = localStorage.getItem("fromLogout");
    if (isLogout === "true") {
      setFromLogout(true); // 로그아웃 플래그가 있으면 fromLogout 상태를 true로 변경
      localStorage.removeItem("fromLogout"); // 플래그는 한 번 읽었으면 바로 삭제 (안 지우면 계속 남아있게 됨)
    }
  }, []);

  // 로그인 유도 경고창
  useEffect(() => {
    if (fromLogout) return; // 로그아웃에서 온 경우(값이 true)라면 confirm 띄우지 않음

    if (!user && !hasPromptedRef.current) {
      hasPromptedRef.current = true; // confirm은 한 번만 띄우기 위해 ref로 체크
      const goLogin = window.confirm(
        "로그인이 필요한 서비스입니다. 로그인 하시겠습니까?"
      );
      if (goLogin) navigate("/login");
      else navigate("/");
    } else if (user && !checkedLogin) {
      setCheckedLogin(true);
    }
  }, [user, checkedLogin, navigate, fromLogout]);

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

  // 댓글 범주화 데이터 (가장 최근 영상 기준)
  useEffect(() => {
    const fetchCategory = async () => {
      try {
        const res = await axios.get(
          `http://localhost:8080/api/comments/category-chart-init?userId=${user.userId}`
        );
        console.log("범주화 응답:", res.data); // [수정]
        const stat = res.data.data.statCountDto;
        if (!stat) {
          console.warn("statCountDto가 null이거나 undefined입니다."); // [수정]
          setCategoryData([]); // [수정]
          return;
        }
        const parsed = Object.entries(stat).map(([key, value]) => ({
          name: key,
          value: Number(value), // 숫자로 변환!
        }));
        setCategoryData(parsed);
      } catch (err) {
        console.error("범주 데이터 로딩 실패:", err);
      }
    };
    if (user?.userId) fetchCategory();
  }, [user]);

  // 댓글 긍부정 데이터
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

  // 콘텐츠 추천 데이터 추가 요청 [수정]
  useEffect(() => {
    const fetchTopics = async () => {
      try {
        const res = await axios.get(
          `http://localhost:8080/api/contents/topic-init?userId=${user.userId}`
        );
        console.log("콘텐츠 추천 응답:", res.data); // [수정]
        setTopics(res.data);
      } catch (err) {
        console.error("콘텐츠 추천 데이터 로딩 실패:", err);
      }
    };
    if (user?.userId) fetchTopics();
  }, [user]);

  // 자동 로그아웃 타이머 추가
  useEffect(() => {
    if (user) {
      const timer = setTimeout(() => {
        logout();
        navigate("/login");
        alert("오랜 시간 활동이 없어 자동으로 로그아웃되었습니다.");
      }, 2 * 60 * 60 * 1000); // setTimeout은 ms 단위

      return () => clearTimeout(timer); // 컴포넌트가 사라지면 타이머 제거
    }
  }, [user]);

  // user가 없고 로그아웃에서 온 게 아닌 경우에는 confirm 띄우기 전에 렌더링을 막는다
  if (!user && !fromLogout) {
    return null;
  }

  if (!user || !checkedLogin) return null;

  // 공통: 미니 차트 옵션
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
        {/* 상단 바 */}
        <TopBar />

        {/* 콘텐츠 카드 영역 */}
        <div className="dashboard">
          <div className="row">
            {/* 구독자 수 카드 */}
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
                    data={{
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
                    }}
                    options={miniChartOptions}
                    height={200}
                  />
                </div>
              )}
            </div>

            {/* 조회수 카드 */}
            <div className="card clickable" onClick={() => navigate("/views")}>
              <h3>조회수 분석</h3>
              {views.length === 0 ? (
                <p style={{ color: "#999", fontSize: "14px" }}>데이터 없음</p>
              ) : (
                <div className="chart-wrapper">
                  <Line
                    data={{
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
                {/* 범주화 차트 */}
                <div
                  className="mini-chart clickable"
                  style={{ flex: 1, textAlign: "center" }}
                  onClick={() => navigate("/category")}
                >
                  {/* <p style={{ fontSize: "14px", marginBottom: "0.5rem" }}>
                    댓글 범주화
                  </p> */}
                  <PieChart width={180} height={180}>
                    <RePie
                      data={categoryData
                        .filter((d) => d.name !== "other") //기타 제외
                        .map((d) => ({
                          name: d.name,
                          value: d.value,
                        }))}
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
                  <p
                    style={{
                      fontSize: "14px",
                      textAlign: "center",
                      marginTop: "0.5rem",
                    }}
                  >
                    댓글 범주화
                  </p>
                </div>

                {/* 긍부정 차트 */}
                <div
                  className="mini-chart clickable"
                  style={{ flex: 1, textAlign: "center" }}
                  onClick={() => navigate("/sentiment")}
                >
                  {/* <p style={{ fontSize: "14px", marginBottom: "0.5rem" }}>
                    긍부정 분석
                  </p> */}
                  <PieChart width={180} height={180}>
                    <RePie
                      data={sentimentData.map((d) => ({
                        name: d.name,
                        value: d.value,
                      }))}
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
                          fill={["#A8CFF2", "#ECB4D6"][i]}
                        />
                      ))}
                    </RePie>
                    <ReTooltip />
                  </PieChart>
                  <p
                    style={{
                      fontSize: "14px",
                      textAlign: "center",
                      marginTop: "0.5rem",
                    }}
                  >
                    긍부정 분석
                  </p>
                </div>
              </div>
            </div>

            {/* 콘텐츠 추천 카드 */}
            <div
              className="card clickable"
              onClick={() => navigate("/recommend")}
            >
              <h3 style={{ marginBottom: "40px" }}>콘텐츠 추천</h3>
              {topics.length === 0 ? (
                <p style={{ color: "#999", fontSize: "14px" }}>데이터 없음</p>
              ) : (
                <ul className="category-rank">
                  {topics.map((topic, idx) => (
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
      </div>
    </div>
  );
};

export default Home;
