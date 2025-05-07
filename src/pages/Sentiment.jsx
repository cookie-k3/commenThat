import React, { useEffect, useState } from "react";
import axios from "axios";
import {
  PieChart,
  Pie,
  Cell,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";
import { useAuth } from "../context/AuthContext";
import LeftHeader from "../components/LeftHeader";
import ReactECharts from "echarts-for-react";
import "echarts-wordcloud"; // 워드클라우드 플러그인
import "../components/sentiment.css";
import VideoSelectTopBar from "../components/VideoSelectTopBar";

const COLORS = ["#A8CFF2", "#ECB4D6"]; // 부정: 연하늘, 긍정: 연보라
const WORD_CLOUD_COLORS = [
  "#ECB4D6", // 연보라핑크
  "#FCE38A", // 연노랑
  "#B388EB", // 보라
  "#D1CFE2", // 회보라
  "#A8CFF2", // 연하늘
  "#76AADB", // 중간 하늘
  "#E4C1F9", // 연한 퍼플
  "#AA96DA", // 보라
  "#C3FBD8", // 민트
  "#FFE0AC", // 살구
];

const Sentiment = () => {
  const { user } = useAuth();
  const [videoId, setVideoId] = useState(null); //선택된 영상 ID
  const [videoList, setVideoList] = useState([]); //전체 영상 리스트
  const [sentimentData, setSentimentData] = useState([]); //긍부정 값
  const [wordCloudData, setWordCloudData] = useState([]); //워드클라우드용 키워드
  const [loading, setLoading] = useState(true); //로딩 상태

  // 워드클라우드용 데이터 정제 함수
  const sanitize = (raw = []) =>
    raw
      .filter(
        (w) =>
          w?.text &&
          typeof w.text === "string" &&
          w.text.trim() &&
          typeof w.value === "number" &&
          isFinite(w.value) &&
          w.value > 0
      )
      .map((w) => ({
        name: w.text.replace(/[<>"'&]/g, "").trim(),
        value: Math.max(1, Math.round(w.value)),
      }))
      .slice(0, 300); // ← 최대 300개까지 허용;

  // 초기 데이터 로딩
  useEffect(() => {
    if (!user?.userId) return;
    axios
      .get(
        `http://localhost:8080/api/comments/senti-chart-init?userId=${user.userId}`
      )
      .then(({ data: dto }) => {
        setVideoId(dto.videoId || null); // null 허용
        setVideoList(dto.videoDtoList || []);
        setSentimentData([
          { name: "부정", value: dto.negativeCount || 0 },
          { name: "긍정", value: dto.positiveCount || 0 },
        ]);
        setWordCloudData(sanitize(dto.positiveCommentDtos || []));
      })
      .catch((e) => {
        console.error(e);
        alert("감정 데이터를 불러오지 못했습니다.");
      })
      .finally(() => setLoading(false));
  }, [user?.userId]);

  // 영상 선택 시 실행
  const handleVideoChange = async (id) => {
    if (id === videoId) return;
    setLoading(true);
    try {
      const { data: dto } = await axios.get(
        `http://localhost:8080/api/comments/senti-chart-videoid?videoId=${id}`
      );
      setVideoId(dto.videoId);
      setSentimentData([
        { name: "부정", value: dto.negativeCount || 0 },
        { name: "긍정", value: dto.positiveCount || 0 },
      ]);
      setWordCloudData(sanitize(dto.positiveCommentDtos || []));
    } catch (e) {
      console.error(e);
      alert("선택한 영상의 감정 데이터를 불러오지 못했습니다.");
    } finally {
      setLoading(false);
    }
  };

  if (!user) return <div>🔐 로그인 정보를 불러오는 중입니다...</div>;
  // if (loading) return <div>📊 감정 데이터를 불러오는 중입니다...</div>;

  // ECharts 옵션 -> 워드클라우드용 설정
  const wordCloudOption = {
    series: [
      {
        type: "wordCloud",
        gridSize: 5,
        sizeRange: [20, 60],
        rotationRange: [0, 90],
        shape: "square",
        textStyle: {
          fontFamily: "Pretendard, sans-serif",
          color: () =>
            WORD_CLOUD_COLORS[
              Math.floor(Math.random() * WORD_CLOUD_COLORS.length)
            ],
        },
        data: wordCloudData,
      },
    ],
  };

  //  감정 데이터 비었는지 확인
  const isSentimentEmpty =
    sentimentData.length === 2 &&
    sentimentData[0].value === 0 &&
    sentimentData[1].value === 0;

  //  워드클라우드 데이터 비었는지 확인
  const isWordCloudEmpty = wordCloudData.length === 0;

  return (
    <div className="home-container">
      <LeftHeader />
      <div className="home-main" style={{ padding: "0 2rem 2rem 2rem" }}>
        {/* 영상 선택 */}
        <VideoSelectTopBar
          fetchUrl="http://localhost:8080/api/comments/senti-chart-init"
          initialVideoId={videoId}
          onVideoSelect={(id) => handleVideoChange(id)}
        />

        {/* 분석 결과 */}
        {videoId === null ? ( //  분석 가능한 영상이 없는 경우 안내
          <p style={{ marginTop: "2rem", textAlign: "center", width: "100%" }}>
            {/* 감정 분석 가능한 영상이 없습니다. */}
          </p>
        ) : (
          <div
            style={{
              display: "flex",
              gap: "2rem",
              flexWrap: "wrap",
              marginTop: "2rem", // 추가된 여백
            }}
          >
            {/* 도넛 차트 카드 */}
            <div
              style={{
                flex: 1,
                minWidth: "300px",
                background: "white",
                borderRadius: "12px",
                boxShadow: "0 2px 10px rgba(0,0,0,0.05)",
                padding: "1rem",
              }}
            >
              <h4 style={{ marginBottom: "1rem" }}>긍부정 차트</h4>
              {isSentimentEmpty ? (
                <p style={{ textAlign: "center", padding: "2rem" }}>
                  감정 분석 데이터가 없습니다.
                </p>
              ) : (
                <ResponsiveContainer width="100%" height={500}>
                  <PieChart>
                    <Pie
                      data={sentimentData}
                      cx="50%"
                      cy="50%"
                      innerRadius={70}
                      outerRadius={120}
                      dataKey="value"
                      label={({ name, value }) => `${name}: ${value}`}
                      labelLine={false}
                    >
                      {sentimentData.map((_, i) => (
                        <Cell key={i} fill={COLORS[i]} />
                      ))}
                    </Pie>
                    <Tooltip />
                    <Legend verticalAlign="bottom" iconType="circle" />
                  </PieChart>
                </ResponsiveContainer>
              )}
            </div>

            {/* 워드클라우드 카드 */}
            <div
              style={{
                flex: 1,
                minWidth: "300px",
                background: "white",
                borderRadius: "12px",
                boxShadow: "0 2px 10px rgba(0,0,0,0.05)",
                padding: "1rem",
                display: "flex",
                flexDirection: "column", // 세로 배치
              }}
            >
              <h4 style={{ marginBottom: "1rem", textAlign: "left" }}>
                긍정 댓글 워드클라우드
              </h4>
              <div
                style={{
                  flex: 1,
                  display: "flex",
                  justifyContent: "center",
                  alignItems: "center",
                }}
              >
                {isWordCloudEmpty ? (
                  <p style={{ textAlign: "center", color: "#888" }}>
                    긍정 댓글이 부족해 워드클라우드를 생성할 수 없습니다.
                  </p>
                ) : (
                  <ReactECharts
                    option={wordCloudOption}
                    style={{ width: "150%", height: 300 }}
                  />
                )}
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default Sentiment;
