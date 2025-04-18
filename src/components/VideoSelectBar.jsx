import React, { useEffect, useState } from "react";
import axios from "axios";
import "../components/Home.css";
import "../components/VideoSelectBar.css";
import { useAuth } from "../context/AuthContext";

// initialVideoId prop 추가
const VideoSelectTopBar = ({ onVideoSelect, initialVideoId }) => {
  const { user } = useAuth();
  const [videos, setVideos] = useState([]);
  const [selectedVideoId, setSelectedVideoId] = useState(null);

  // 로그아웃 함수 정의
  const handleLogout = () => {
    localStorage.removeItem("user"); // 저장된 로그인 정보 제거
    window.location.href = "/login"; // 로그인 페이지로 이동
  };

  useEffect(() => {
    const fetchVideos = async () => {
      try {
        const res = await axios.get(
          `http://localhost:8080/api/comments/category-chart-init?userId=${user.userId}`
        );

        const videoList = res.data.data.videoDtoList;
        console.log("받은 영상 목록:", videoList);

        setVideos(videoList);

        // 전달받은 initialVideoId 우선 사용, 없으면 기본 로직 사용
        const defaultVideoId =
          initialVideoId || res.data.data.videoId || videoList?.[0]?.videoId;

        if (defaultVideoId) {
          setSelectedVideoId(defaultVideoId);
          onVideoSelect(defaultVideoId);
        }
      } catch (e) {
        console.error("영상 목록 조회 실패:", e);
      }
    };

    if (user?.userId) fetchVideos();
  }, [user, initialVideoId]); //  dependency에 initialVideoId 추가

  const handleChange = (e) => {
    const videoId = Number(e.target.value);
    setSelectedVideoId(videoId);
    onVideoSelect(videoId);
  };

  return (
    <div className="home-topbar-wrapper">
      <div className="home-topbar">
        {/* 영상 선택 드롭다운 */}
        <div className="video-select-bar">
          <select value={selectedVideoId || ""} onChange={handleChange}>
            <option value="" disabled>
              영상 선택
            </option>
            {videos.map((video) => (
              <option key={`video-${video.videoId}`} value={video.videoId}>
                {video.videoTitle}
              </option>
            ))}
          </select>
          <button>search</button>
        </div>

        {/* 유저 정보 + 로그아웃 */}
        <div className="user-info">
          <span>{user?.loginId}</span>
          <span>|</span>
          {/* 로그아웃 클릭 시 handleLogout 호출 */}
          <span style={{ cursor: "pointer" }} onClick={handleLogout}>
            로그아웃
          </span>
        </div>
      </div>
    </div>
  );
};

export default VideoSelectTopBar;
