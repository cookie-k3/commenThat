import React from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import "./LeftHeader.css";
import logo from "../assets/logo.png";
import subscriberIcon from "../assets/subscriber.png";
import viewIcon from "../assets/view.png";
import sentimentIcon from "../assets/sentiment.png";
import ideaIcon from "../assets/idea.png";
import category from "../assets/category.png";

const LeftHeader = () => {
  const navigate = useNavigate();
  // const { logout } = useAuth();

  // const handleLogout = () => {
  //   const confirmLogout = window.confirm("로그아웃하시겠습니까?");
  //   if (confirmLogout) {
  //     logout(); // user 상태 초기화 및 localStorage에서 user 삭제
  //     localStorage.setItem("fromLogout", "true"); // 로그아웃 여부 저장 (Home에서 confirm 방지용)
  //     navigate("/"); // 메인 페이지로 이동
  //   }
  // };

  return (
    <div className="left-header">
      <img
        src={logo}
        alt="Logo"
        className="side-logo"
        onClick={() => navigate("/home")}
      />

      <div className="menu-wrapper">
        {" "}
        {/* 중앙 정렬용 래퍼 */}
        <div className="menu-section">
          <p className="section-title">채널 분석</p>
          <button onClick={() => navigate("/views")}>
            <img src={subscriberIcon} alt="구독자" className="icon" />
            채널 조회수
          </button>
          <button onClick={() => navigate("/views/by-video")}>
            <img src={viewIcon} alt="조회수" className="icon" />
            영상별 조회수
          </button>
        </div>
        <div className="menu-section">
          <p className="section-title">댓글 분석</p>
          <button onClick={() => navigate("/category")}>
            <img src={category} alt="범주화" className="icon" />
            범주화
          </button>
          <button onClick={() => navigate("/sentiment")}>
            <img src={sentimentIcon} alt="긍부정" className="icon" />
            긍부정
          </button>
        </div>
        <div className="menu-section">
          <p className="section-title">아이디어</p>
          <button onClick={() => navigate("/recommend")}>
            <img src={ideaIcon} alt="콘텐츠 추천" className="icon" />
            콘텐츠 추천
          </button>
        </div>
      </div>
    </div>
  );
};

export default LeftHeader;
