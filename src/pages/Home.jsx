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

  //가장 최근 영상부터 조회해서 7일치 조회수 데이터가 있는 영상 선택
  useEffect(() => {
    const fetchVideoTrend = async () => {
      try {
        const res1 = await axios.get(
          `http://localhost:8080/api/analysis/view-chart-init?userId=${user.userId}`
        );

        const videoList = res1.data?.data?.videoDtoList ?? [];
        console.log("영상 목록:", videoList);

        for (const video of videoList) {
          const videoId = video.videoId;
          const title = video.videoTitle;

          const res2 = await axios.get(
            `http://localhost:8080/api/analysis/video-views?videoId=${videoId}`
          );

          const sorted = [...res2.data].sort(
            (a, b) => new Date(a.date) - new Date(b.date)
          );

          if (sorted.length >= 7) {
            console.log("선택된 대표 영상:", title);
            console.log("[대표 영상 그래프] 최종 데이터", sorted.slice(-7));
            setVideoTrendData(sorted.slice(-7));
            break; // 첫 번째로 조건 만족하는 영상만 사용
          }
        }
      } catch (e) {
        console.error("영상별 조회수 미리보기 로딩 실패:", e);
      }
    };

    if (user?.userId) fetchVideoTrend();
  }, [user]);

  // 가장 최근 영상 목록 중에서 카테고리(statCountDto) 값이 모두 0이 아닌 첫 번째 영상을 찾아 차트에 표시
  useEffect(() => {
    const fetchCategory = async () => {
      try {
        const res1 = await axios.get(
          `http://localhost:8080/api/analysis/view-chart-init?userId=${user.userId}`
        );
        const videoList = res1.data?.data?.videoDtoList ?? [];
        console.log("전체 영상 목록 확인:", videoList);

        for (const video of videoList) {
          try {
            const res = await axios.get(
              `http://localhost:8080/api/comments/category-chart-videoid?videoId=${video.videoId}`
            );

            console.log("[범주화] 응답:", res.data);

            const stat = res.data?.statCountDto; // statCountDto 직접 접근
            console.log("[범주화] statCountDto:", stat);

            if (!stat) {
              console.log(" statCountDto가 존재하지 않습니다");
              continue;
            }

            const values = Object.values(stat).map(Number);
            console.log("[범주화] 값 배열:", values);

            // 하나라도 0보다 크면 차트로 표시
            if (values.some((val) => val > 0)) {
              const parsed = Object.entries(stat).map(([key, value]) => ({
                name: key,
                value: Number(value),
              }));
              setCategoryData(parsed);
              console.log(" 범주화 분석 사용된 영상:", video.videoTitle);
              break;
            } else {
              console.log(
                ` ${video.videoTitle} 영상은 카테고리 데이터가 모두 0`
              );
            }
          } catch (innerErr) {
            console.error(
              ` ${video.videoTitle} 영상 처리 중 에러 발생:`,
              innerErr
            );
          }
        }
      } catch (err) {
        console.error("범주 데이터 로딩 실패:", err);
      }
    };

    if (user?.userId) fetchCategory();
  }, [user]);

  useEffect(() => {
    console.log("카테고리 데이터:", categoryData);
  }, [categoryData]);

  // 영상 목록 중 긍정+부정 댓글 수의 합이 0보다 큰 첫 번째 영상을 선택하여 도넛차트에 표시
  useEffect(() => {
    const fetchSentiment = async () => {
      try {
        const res1 = await axios.get(
          `http://localhost:8080/api/analysis/view-chart-init?userId=${user.userId}`
        );
        const videoList = res1.data?.data?.videoDtoList ?? [];

        for (const video of videoList) {
          const res = await axios.get(
            `http://localhost:8080/api/comments/senti-chart-videoid?videoId=${video.videoId}` // ✅ 경로 수정
          );
          const data = res.data;

          if (data.positiveCount + data.negativeCount > 0) {
            setSentimentData([
              { name: "부정", value: data.negativeCount },
              { name: "긍정", value: data.positiveCount },
            ]);
            console.log("긍부정 분석 사용된 영상:", video.videoTitle);
            break;
          }
        }
      } catch (err) {
        console.error("긍부정 데이터 로딩 실패:", err);
      }
    };
    if (user?.userId) fetchSentiment();
  }, [user]);

  //기존 영상별조회수 카드 부분 코드
  // useEffect(() => {
  //   const fetchVideoTrend = async () => {
  //     try {
  //       // 1. 최근 영상 ID 조회
  //       const res1 = await axios.get(
  //         `http://localhost:8080/api/comments/senti-chart-init?userId=${user.userId}`
  //       );
  //       const recentVideoId = res1.data.videoId;

  //       if (!recentVideoId) return;

  //       // 2. 해당 영상에 대한 조회수 추이 조회
  //       const res2 = await axios.get(
  //         `http://localhost:8080/api/analysis/video-views?videoId=${recentVideoId}`
  //       );

  //       const sorted = [...res2.data].sort(
  //         (a, b) => new Date(a.date) - new Date(b.date)
  //       );
  //       setVideoTrendData(sorted.slice(-7)); // 최근 7일치만
  //     } catch (e) {
  //       console.error("영상별 조회수 미리보기 로딩 실패:", e);
  //     }
  //   };

  //   if (user?.userId) fetchVideoTrend();
  // }, [user]);

  // useEffect(() => {
  //   const fetchCategory = async () => {
  //     try {
  //       const res = await axios.get(
  //         `http://localhost:8080/api/comments/category-chart-init?userId=${user.userId}`
  //       );
  //       const stat = res.data.data.statCountDto;
  //       if (!stat) {
  //         setCategoryData([]);
  //         return;
  //       }
  //       const parsed = Object.entries(stat).map(([key, value]) => ({
  //         name: key,
  //         value: Number(value),
  //       }));
  //       setCategoryData(parsed);
  //     } catch (err) {
  //       console.error("범주 데이터 로딩 실패:", err);
  //     }
  //   };
  //   if (user?.userId) fetchCategory();
  // }, [user]);

  // useEffect(() => {
  //   const fetchSentiment = async () => {
  //     try {
  //       const res = await axios.get(
  //         `http://localhost:8080/api/comments/senti-chart-init?userId=${user.userId}`
  //       );
  //       const data = res.data;
  //       setSentimentData([
  //         { name: "부정", value: data.negativeCount },
  //         { name: "긍정", value: data.positiveCount },
  //       ]);
  //     } catch (err) {
  //       console.error("긍부정 데이터 로딩 실패:", err);
  //     }
  //   };
  //   if (user?.userId) fetchSentiment();
  // }, [user]);

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
                          // borderColor: "rgba(75, 192, 192, 1)",
                          borderColor: "rgba(255, 99, 132, 1)",
                          // backgroundColor: "rgba(75, 192, 192, 0.2)",
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
                  {(() => {
                    const filteredCategoryData = categoryData
                      .filter((d) => d.name !== "other")
                      .map((d) => ({ name: d.name, value: d.value }));

                    return filteredCategoryData.length === 0 ? (
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
                      <>
                        <PieChart width={180} height={180}>
                          <RePie
                            data={filteredCategoryData}
                            cx="50%"
                            cy="50%"
                            outerRadius={80}
                            dataKey="value"
                            labelLine={false}
                          >
                            {filteredCategoryData.map((_, i) => (
                              <Cell
                                key={`cat-${i}`}
                                fill={
                                  [
                                    "#FAD6E5", // 연핑크
                                    "#AED9E0", // 연하늘
                                    "#B2E2C2", // 연민트
                                    "#FFF2AC", // 연노랑
                                    "#C1BBDD", // 연보라
                                    "#A2D2FF", // 맑은 하늘색
                                    "#FFC8A2", // 살구색
                                    "#B5EAD7", // 민트+연녹색
                                    "#FFDAC1", // 연살구핑크
                                    "#E2F0CB", // 연한 연두
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
                      </>
                    );
                  })()}
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
            <div
              className="card clickable"
              onClick={() => navigate("/recommend")}
            >
              <h3 style={{ marginBottom: "0px" }}>콘텐츠 추천</h3>

              {summary && summary.topViewVideo && (
                <div className="mini-summary-box">
                  <div>한 달간 최고 조회수 영상</div>
                  <div className="mini-video-row">
                    <img
                      src={JSON.parse(summary.topViewVideo).thumbnail}
                      alt="썸네일"
                      className="mini-thumbnail"
                    />
                    <div>
                      <div className="mini-title">
                        {JSON.parse(summary.topViewVideo).title}
                      </div>
                      <div style={{ fontSize: "13px", color: "#555" }}>
                        조회수:{" "}
                        {Number(
                          JSON.parse(summary.topViewVideo).views
                        ).toLocaleString()}
                        회
                      </div>
                    </div>
                  </div>
                </div>
              )}

              <div className="recommend-label">추천 키워드</div>

              {topics.length === 0 ? (
                <div className="no-topic">데이터 없음</div>
              ) : (
                <div className="horizontal-topic-list">
                  {topics.slice(0, 2).map((topic, idx) => (
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
