import React, { useState, useEffect } from "react";
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

// const ViewByVideo = () => {
//   const { user } = useAuth();
//   const [videoId, setVideoId] = useState(null);
//   const [viewData, setViewData] = useState([]);

//   const handleVideoSelect = async (selectedId) => {
//     setVideoId(selectedId);

//     try {
//       const res = await axios.get(
//         `/api/analysis/video-views?videoId=${selectedId}`
//       );
//       const raw = res.data;

//       console.log("[서버 응답] 원본 raw 데이터:", raw);

//       // 날짜 오름차순 정렬
//       const sorted = [...raw].sort(
//         (a, b) => new Date(a.date) - new Date(b.date)
//       );
//       console.log("[정렬된 데이터]", sorted);

//       setViewData(sorted);
//     } catch (err) {
//       console.error("영상별 조회수 데이터 로딩 실패:", err);
//     }
//   };

//   const chartData = {
//     labels: viewData.map((d) => d.date),
//     datasets: [
//       {
//         label: "누적 조회수",
//         data: viewData.map((d) => d.views),
//         borderColor: "rgba(75, 192, 192, 1)",
//         backgroundColor: "rgba(75, 192, 192, 0.2)",
//         fill: true,
//         tension: 0.3,
//       },
//     ],
//   };

//   const options = {
//     responsive: true,
//     plugins: {
//       legend: {
//         position: "top",
//       },
//     },
//   };

//   return (
//     <div className="home-container">
//       <LeftHeader />
//       <div className="home-main">
//         {/* 영상 선택 상단 바 */}
//         <VideoSelectTopBar
//           fetchUrl={`/api/analysis/view-chart-init`}
//           onVideoSelect={handleVideoSelect}
//           initialVideoId={null}
//         />

//         <div className="subscriber-container">
//           <h2>영상별 조회수 추이</h2>
//           {viewData.length === 0 ? (
//             <p style={{ color: "#999", fontSize: "14px" }}>데이터 없음</p>
//           ) : (
//             <Line data={chartData} options={options} />
//           )}
//         </div>
//       </div>
//     </div>
//   );
// };

// export default ViewByVideo;

const ViewByVideo = () => {
  const { user } = useAuth();
  const [videoId, setVideoId] = useState(null);
  const [viewData, setViewData] = useState([]);

  // ① 마운트 시: 최신 영상부터 순회하며 조회 데이터가 7개 이상인 영상 찾아 초기 선택
  useEffect(() => {
    const initSelect = async () => {
      try {
        // 1) 사용자 영상 목록 (최신순) 가져오기
        const resList = await axios.get(
          `/api/analysis/view-chart-init?userId=${user.userId}`
        );
        const videoList = resList.data?.data?.videoDtoList ?? [];
        console.log("전체 영상 리스트:", videoList);

        // 2) 영상 하나씩 조회해서 7일치 이상 데이터 있는 첫 영상 선택
        for (const { videoId: vid } of videoList) {
          const resViews = await axios.get(
            `/api/analysis/video-views?videoId=${vid}`
          );
          const sorted = [...resViews.data].sort(
            (a, b) => new Date(a.date) - new Date(b.date)
          );
          if (sorted.length >= 7) {
            console.log(
              "초기 선택 영상Id:",
              vid,
              "데이터 개수:",
              sorted.length
            );
            handleVideoSelect(vid);
            break;
          }
        }
      } catch (err) {
        console.error("초기 대표 영상 선택 실패:", err);
      }
    };

    initSelect();
  }, [user.userId]);

  // ② 영상 선택 처리
  const handleVideoSelect = async (selectedId) => {
    setVideoId(selectedId);

    try {
      const res = await axios.get(
        `/api/analysis/video-views?videoId=${selectedId}`
      );
      const raw = res.data;

      // 날짜 오름차순 정렬
      const sorted = [...raw].sort(
        (a, b) => new Date(a.date) - new Date(b.date)
      );
      setViewData(sorted);
    } catch (err) {
      console.error("영상별 조회수 데이터 로딩 실패:", err);
    }
  };

  // ③ 차트 데이터 준비
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
      legend: { position: "top" },
    },
  };

  return (
    <div className="home-container">
      <LeftHeader />
      <div className="home-main">
        {/* VideoSelectTopBar 에는 fetchUrl 과 onVideoSelect 만 전달 */}
        <VideoSelectTopBar
          fetchUrl={`/api/analysis/view-chart-init`}
          onVideoSelect={handleVideoSelect}
          initialVideoId={videoId}
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
