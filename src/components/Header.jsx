import { useNavigate } from "react-router-dom";
import "./Header.css";
import logo from "../assets/logo.png";

const Header = () => {
  const navigate = useNavigate(); // useNavigate 훅 사용

  return (
    <header className="header">
      <div className="header-container">
        {/* 좌측: 로고 */}
        <div className="logo" onClick={() => navigate("/")}>
          {" "}
          {/*  클릭 시 메인 페이지 이동 */}
          <img src={logo} alt="CommentHAT Logo" style={{ cursor: "pointer" }} />
        </div>

        {/* 중앙: 네비게이션 메뉴 */}
        <nav className="nav">
          <span onClick={() => navigate("/about")} className="nav-item">
            소개
          </span>
          <span onClick={() => navigate("/features")} className="nav-item">
            기능
          </span>
          <span onClick={() => navigate("/analysis")} className="nav-item">
            분석
          </span>
        </nav>

        {/* 우측: 로그인 및 가입 버튼 */}
        <div className="auth-buttons">
          <span onClick={() => navigate("/signup")} className="join">
            Join us
          </span>
          <span onClick={() => navigate("/login")} className="login">
            Log in
          </span>
        </div>
      </div>
    </header>
  );
};

export default Header;
