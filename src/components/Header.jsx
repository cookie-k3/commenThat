import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext"; // 로그인 정보
import "./Header.css";
import logo from "../assets/logo.png";

const Header = () => {
  const navigate = useNavigate();
  const { user } = useAuth(); // 로그인 상태 가져오기

  return (
    <header className="header">
      <div className="header-container">
        {/* 좌측: 로고 */}
        <div className="logo" onClick={() => navigate("/")}>
          <img src={logo} alt="CommentHAT Logo" style={{ cursor: "pointer" }} />
        </div>

        {/* 중앙: 네비게이션 메뉴 */}
        <nav className="nav">
          <span onClick={() => navigate("/about")} className="nav-item">
            소개
          </span>
          <span
            onClick={() => navigate("/analysis_center")}
            className="nav-item"
          >
            분석센터
          </span>
        </nav>

        {/* 우측: 로그인 상태에 따라 변경 */}
        <div className="auth-buttons">
          {user ? (
            <span className="login-id">{user.loginId}님</span> //  로그인 시 아이디 출력
          ) : (
            <>
              <span onClick={() => navigate("/signup")} className="join">
                Join us
              </span>
              <span onClick={() => navigate("/login")} className="login">
                Log in
              </span>
            </>
          )}
        </div>
      </div>
    </header>
  );
};

export default Header;
