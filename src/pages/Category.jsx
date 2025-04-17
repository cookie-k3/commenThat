import React, { useEffect, useState } from "react";
import axios from "axios";
import { PieChart, Pie, Cell, Tooltip, Legend } from "recharts";
import { useNavigate } from "react-router-dom";
import "../components/Category.css";
import LeftHeader from "../components/LeftHeader";
import TopBar from "../components/TopBar";
import { useAuth } from "../context/AuthContext";

// 영어 key ↔ 한글 라벨 매핑
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

const COLORS = [
  "#FF9999",
  "#FFCC99",
  "#FFFF99",
  "#CCFF99",
  "#99FFCC",
  "#99CCFF",
  "#9999FF",
  "#CC99FF",
  "#FF99CC",
  "#FF6666",
  "#66CCCC",
  "#CCCC66",
  "#9966CC",
  "#666699",
];

const Category = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [videoId, setVideoId] = useState(null);
  const [videoList, setVideoList] = useState([]); // 드롭다운용 영상 목록
  const [categoryStats, setCategoryStats] = useState({});
  const [topCategories, setTopCategories] = useState([]);
  const [selectedCategoryId, setSelectedCategoryId] = useState(null);
  const [comments, setComments] = useState([]);
  const [summary, setSummary] = useState("");

  // 초기 데이터 로딩: 최근 영상 + 전체 영상 목록
  useEffect(() => {
    const fetchInitialStats = async () => {
      try {
        const res = await axios.get(
          `http://localhost:8080/api/comments/category-chart-init?userId=${user.userId}`
        );
        const dto = res.data.data;
        setVideoId(dto.videoId);
        setVideoList(dto.videoDtoList);
        setCategoryStats(dto.statCountDto);

        const sorted = Object.entries(dto.statCountDto)
          .sort((a, b) => Number(b[1]) - Number(a[1]))
          .slice(0, 8);
        setTopCategories(sorted);
      } catch (e) {
        alert("범주 데이터를 불러오지 못했습니다.");
      }
    };

    if (user?.userId) fetchInitialStats();
  }, [user]);

  // 영상 선택 시 범주 재조회
  const handleVideoSelect = async (selectedId) => {
    try {
      const res = await axios.get(
        `http://localhost:8080/api/comments/category-chart-videoid?videoId=${selectedId}`
      );
      setVideoId(res.data.videoId);
      setCategoryStats(res.data.statCountDto);
      setSelectedCategoryId(null);

      const sorted = Object.entries(res.data.statCountDto)
        .sort((a, b) => Number(b[1]) - Number(a[1]))
        .slice(0, 8);
      setTopCategories(sorted);
    } catch (e) {
      alert("선택한 영상의 범주 데이터를 불러오지 못했습니다.");
    }
  };

  // 파이 차트 데이터 변환
  const pieData = Object.entries(categoryStats).map(([key, value]) => ({
    name: categoryKeyMap[key],
    value: Number(value),
  }));

  return (
    <div className="home-container">
      <LeftHeader />
      <div className="home-main">
        <TopBar hideDateRange={true} />

        <div className="category-section">
          <h3>댓글 범주화</h3>

          {/* ✅ 영상 선택 드롭다운 */}
          <select
            value={videoId || ""}
            onChange={(e) => handleVideoSelect(e.target.value)}
          >
            {videoList.map((video) => (
              <option key={video.id} value={video.id}>
                {video.title}
              </option>
            ))}
          </select>

          <div className="category-chart-rank">
            {/* 좌측: 원형 차트 */}
            <div className="pie-chart-box">
              <PieChart width={500} height={500}>
                <Pie
                  data={pieData}
                  cx="50%"
                  cy="50%"
                  outerRadius={150}
                  dataKey="value"
                  label={({ name, percent }) =>
                    `${name} (${(percent * 100).toFixed(0)}%)`
                  }
                >
                  {pieData.map((entry, index) => (
                    <Cell key={index} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
                <Legend />
              </PieChart>
            </div>

            {/* 우측: 범주 순위 */}
            <div className="category-rank-box">
              <h4>댓글 범주화 순위</h4>
              <ul className="category-rank">
                {topCategories.map(([key, value], idx) => (
                  <li key={key}>
                    <span className="rank-num">{idx + 1}</span>
                    <span className="rank-label">{categoryKeyMap[key]}</span>
                    <button
                      onClick={() =>
                        navigate(
                          `/category/detail?videoId=${videoId}&categoryId=${
                            idx + 1
                          }`
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
