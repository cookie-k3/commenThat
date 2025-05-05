import React, { useEffect, useState } from "react";
import axios from "axios";
import "../components/Home.css";
import "../components/VideoSelectTopBar.css";
import { useAuth } from "../context/AuthContext";

// fetchUrl, initialVideoId prop 추가
// fetchUrl: 영상 목록을 가져올 API URL
// initialVideoId: 최초 선택될 영상 ID
// onVideoSelect: 영상 선택 시 상위 컴포넌트에서 실행할 함수
const VideoSelectTopBar = ({ fetchUrl, onVideoSelect, initialVideoId }) => {
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
        const res = await axios.get(`${fetchUrl}?userId=${user.userId}`);

        // 추가 설명: API 응답 구조가 서비스마다 다르므로 유연하게 처리함
        // 예: res.data.videoDtoList 또는 res.data.data.videoDtoList 형태일 수 있음
        const raw = res?.data || {};
        const videoList = raw.videoDtoList || raw.data?.videoDtoList || [];

        console.log("받은 영상 목록:", videoList);

        setVideos(videoList);

        // defaultVideoId는 아래 우선순위에 따라 결정됨:
        // 1. 상위 컴포넌트에서 전달된 initialVideoId
        // 2. API 응답의 videoId (대표 영상 ID)
        // 3. videoDtoList 배열의 첫 번째 요소의 videoId
        const defaultVideoId =
          initialVideoId ||
          raw.videoId ||
          raw.data?.videoId ||
          videoList[0]?.videoId;

        if (defaultVideoId) {
          setSelectedVideoId(defaultVideoId);
          onVideoSelect(defaultVideoId); // 상위 컴포넌트에 선택된 videoId 전달
        }
      } catch (e) {
        console.error("영상 목록 조회 실패:", e);
      }
    };

    if (user?.userId && fetchUrl) fetchVideos();
  }, [user, fetchUrl, initialVideoId]); // fetchUrl, initialVideoId를 의존성에 포함

  const handleChange = (e) => {
    const videoId = Number(e.target.value);
    setSelectedVideoId(videoId);
    onVideoSelect(videoId); // 변경된 영상 ID 전달
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
