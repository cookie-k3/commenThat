import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import Header from "../components/Header";
import "../components/Main.css";

import chatImage from "../assets/chat.png";
import mashBackground from "../assets/mash.png";

const Main = () => {
  const navigate = useNavigate();
  const { user } = useAuth();

  return (
    <div className="main" style={{ backgroundImage: `url(${mashBackground})` }}>
      <Header />
      <div className="main-content">
        <img src={chatImage} alt="Chat Icon" className="chat-image" />
        <h1>댓글을 분석해 볼까요?</h1>
        <p>인사이트를 얻어보세요.✨ 채널 평판을 구축하세요.📊</p>
        <div style={{ marginBottom: "20px", fontWeight: "bold" }}>
          {user && `${user.loginId}님 환영합니다!`}
        </div>
        <button className="start-button" onClick={() => navigate("/home")}>
          시작하기
        </button>
      </div>
    </div>
  );
};

export default Main;
