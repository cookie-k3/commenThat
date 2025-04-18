import React, { useState, useEffect } from "react";
import axios from "axios";
import { PieChart, Pie, Cell, Tooltip, Legend } from "recharts";
import { useNavigate, useLocation } from "react-router-dom"; // 🔄 location 추가
import "../components/Category.css";
import LeftHeader from "../components/LeftHeader";
import VideoSelectBar from "../components/VideoSelectBar";
import { useAuth } from "../context/AuthContext";

// 범주 영어-한글 변환
const categoryKeyMap = {
  joy: "즐거움",
  sadness: "슬픔",
  anger: "분노",
  fear: "무서움",
  happiness: "행복",
  cheering: "응원",
  concern: "걱정",
  sympathy: "공감",
  congratulations: "축하",
  question: "질문",
  suggestion: "요청",
  praise: "칭찬",
  hate: "혐오",
  other: "기타",
};

// 범주 영어 → ID 매핑
const categoryIdMap = {
  joy: 1,
  sadness: 2,
  anger: 3,
  fear: 4,
  happiness: 5,
  cheering: 6,
  concern: 7,
  sympathy: 8,
  congratulations: 9,
  question: 10,
  suggestion: 11,
  praise: 12,
  hate: 13,
  other: 14,
};

const COLORS = [
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
];

const RADIAN = Math.PI / 180;

// 사용자 지정 라벨 컴포넌트 (작대기 포함, 텍스트 색상도 Cell 색상과 일치)
const renderCustomizedLabel = ({
  cx,
  cy,
  midAngle,
  outerRadius,
  percent,
  name,
  rawKey,
  top3Keys,
  fill,
}) => {
  if (!top3Keys.includes(rawKey)) return null;

  const startRadius = outerRadius;
  const endRadius = outerRadius + 20;
  const angle = -midAngle * RADIAN;

  const sx = cx + startRadius * Math.cos(angle); // 선 시작점
  const sy = cy + startRadius * Math.sin(angle);
  const ex = cx + endRadius * Math.cos(angle); // 선 끝점 (라벨 위치)
  const ey = cy + endRadius * Math.sin(angle);

  // 라벨을 선 끝점보다 더 바깥쪽에 위치시키기 (여기서 거리 벌려줌)
  const tx = cx + (outerRadius + 25) * Math.cos(angle);
  const ty = cy + (outerRadius + 25) * Math.sin(angle);

  return (
    <g>
      <line x1={sx} y1={sy} x2={ex} y2={ey} stroke={fill} strokeWidth={1.5} />
      <text
        x={tx}
        y={ty}
        fill={fill}
        textAnchor={ex > cx ? "start" : "end"}
        dominantBaseline="central"
        fontSize={14}
        fontWeight={600}
      >
        {`${name} (${Math.round(percent * 100)}%)`}
      </text>
    </g>
  );
};

const Category = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const location = useLocation(); // 현재 URL 확인용
  const queryParams = new URLSearchParams(location.search);
  const initialVideoId = queryParams.get("videoId"); // URL에서 videoId 추출

  const [videoId, setVideoId] = useState(null);
  const [categoryStats, setCategoryStats] = useState({});
  const [topCategories, setTopCategories] = useState([]);
  const [excludeOther, setExcludeOther] = useState(false);

  // 영상 선택 시 범주 통계 로딩
  const handleVideoSelect = async (selectedId) => {
    try {
      const res = await axios.get(
        `http://localhost:8080/api/comments/category-chart-videoid?videoId=${selectedId}`
      );
      setVideoId(res.data.videoId); // 현재 선택된 영상 ID 저장
      setCategoryStats(res.data.statCountDto);

      // 상위 8개 범주 추출
      const sorted = Object.entries(res.data.statCountDto)
        .sort((a, b) => Number(b[1]) - Number(a[1]))
        .slice(0, 8);
      setTopCategories(sorted);
    } catch (e) {
      alert("범주 데이터 로딩 실패");
    }
  };

  // 처음 마운트되었을 때 URL에서 videoId가 있으면 자동 선택
  useEffect(() => {
    if (initialVideoId) {
      handleVideoSelect(Number(initialVideoId));
    }
  }, [initialVideoId]);

  // 그래프에 보여줄 데이터 구성 (기타 제외 옵션 반영)
  const filteredPieData = Object.entries(categoryStats)
    .filter(([key]) => !excludeOther || key !== "other")
    .map(([key, value]) => ({
      name: categoryKeyMap[key],
      value: Number(value),
      rawKey: key,
    }));

  // top3Keys는 filteredPieData 기준으로 계산 (기타 제외 옵션 반영)
  const top3Keys = [...filteredPieData]
    .sort((a, b) => b.value - a.value)
    .slice(0, 3)
    .map((item) => item.rawKey);

  return (
    <div className="home-container">
      <LeftHeader />
      <div className="home-main">
        {/* 영상 선택 바 */}
        <VideoSelectBar
          userId={user?.userId}
          onVideoSelect={handleVideoSelect}
          initialVideoId={initialVideoId}
        />

        <div className="category-section">
          <div className="category-chart-rank">
            {/*  왼쪽: 원형 차트 카드 */}
            <div className="pie-chart-box">
              <h3>댓글 범주화</h3>

              {/* 기타 제외 체크박스 */}
              <div style={{ marginBottom: "10px" }}>
                <label>
                  <input
                    type="checkbox"
                    checked={excludeOther}
                    onChange={() => setExcludeOther(!excludeOther)}
                  />{" "}
                  기타 제외해서 보기
                </label>
              </div>

              <PieChart width={500} height={500}>
                <Pie
                  data={filteredPieData}
                  cx="50%"
                  cy="50%"
                  outerRadius={150}
                  dataKey="value"
                  // 상위 3개만 작대기 표시
                  labelLine={(entry) => top3Keys.includes(entry.rawKey)}
                  // 상위 3개만 라벨 표시, 직접 작대기와 함께 그려줌
                  label={(props) =>
                    renderCustomizedLabel({ ...props, top3Keys })
                  }
                >
                  {filteredPieData.map((entry, index) => (
                    <Cell
                      key={`cell-${index}`}
                      fill={
                        COLORS[
                          Object.keys(categoryKeyMap).indexOf(entry.rawKey) %
                            COLORS.length
                        ]
                      }
                    />
                  ))}
                </Pie>
                <Tooltip
                  formatter={(value, name) => {
                    const count = value;
                    const percent = (
                      (count /
                        filteredPieData.reduce(
                          (acc, cur) => acc + cur.value,
                          0
                        )) *
                      100
                    ).toFixed(0);
                    return [`${percent}% (${count}개)`, name];
                  }}
                />
                <Legend />
              </PieChart>
            </div>

            {/* 오른쪽: 범주 순위 카드 */}
            <div className="category-rank-box">
              <h3>댓글 범주화 순위</h3>
              <ul className="category-rank">
                {topCategories.map(([key, value], idx) => (
                  <li key={key}>
                    <span className="rank-num">{idx + 1}</span>
                    <span className="rank-label">{categoryKeyMap[key]}</span>
                    <button
                      onClick={() =>
                        navigate(
                          `/category/detail?videoId=${videoId}&categoryId=${categoryIdMap[key]}`
                        )
                      }
                    >
                      자세히 보기
                    </button>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Category;
