// src/components/VideoSelectBar.jsx
import React, { useEffect, useState } from "react";
import axios from "axios";
import "./VideoSelectBar.css"; // 필요한 경우 스타일 따로 관리
import { FaVideo } from "react-icons/fa"; // 영상 아이콘

const VideoSelectBar = ({ userId, onVideoSelect }) => {
  const [videos, setVideos] = useState([]);
  const [selectedVideoId, setSelectedVideoId] = useState(null);

  useEffect(() => {
    const fetchVideos = async () => {
      try {
        const res = await axios.get(
          `http://localhost:8080/api/comments/category-chart-init?userId=${userId}`
        );
        setVideos(res.data.data.videoDtoList);
        setSelectedVideoId(res.data.data.videoId);
        onVideoSelect(res.data.data.videoId); // 기본 선택
      } catch (e) {
        console.error("영상 목록 조회 실패:", e);
      }
    };

    if (userId) fetchVideos();
  }, [userId]);

  const handleChange = (e) => {
    const videoId = Number(e.target.value);
    setSelectedVideoId(videoId);
    onVideoSelect(videoId); // 부모(Category)에 전달
  };

  return (
    <div className="video-select-bar">
      <select value={selectedVideoId || ""} onChange={handleChange}>
        <option value="" disabled>
          영상 선택
        </option>
        {videos.map((video) => (
          <option key={video.id} value={video.id}>
            {video.title}
          </option>
        ))}
      </select>
      <button>search</button>
    </div>
  );
};

export default VideoSelectBar;
