import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "../components/Login.css";
import hatImage from "../assets/hat.png";
import logo from "../assets/logo.png";
import axios from "axios";
import { useAuth } from "../context/AuthContext";

const Login = () => {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [loginId, setLoginId] = useState("");
  const [password, setPassword] = useState("");

  const handleLogin = async () => {
    if (!loginId || !password) {
      alert("아이디와 비밀번호를 입력해 주세요.");
      return;
    }

    try {
      const response = await axios.post(
        "http://localhost:8080/api/users/login",
        {
          loginId,
          password,
        }
      );

      // 성공 여부 확인
      if (response.data.success) {
        alert(response.data.message); // "로그인 성공!"
        login({ loginId }); // Context에 로그인 상태 저장
        navigate("/home"); // 홈으로 이동
      } else {
        alert(response.data.message); // ex) "아이디 또는 비밀번호가 올바르지 않습니다."
      }
    } catch (error) {
      if (
        error.response &&
        error.response.data &&
        typeof error.response.data.message === "string"
      ) {
        alert(error.response.data.message); // 서버에서 보낸 메시지 출력
      } else {
        alert("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
      }
      console.error("로그인 에러:", error);
    }
  };

  return (
    <div className="login-container">
      <div className="login-left">
        <img src={hatImage} alt="hat" />
      </div>

      <div className="login-right">
        <div className="login-box">
          <img
            src={logo}
            alt="logo"
            className="logo"
            onClick={() => navigate("/")}
          />

          <input
            type="text"
            placeholder="아이디를 입력해 주세요."
            value={loginId}
            onChange={(e) => setLoginId(e.target.value)}
          />
          <input
            type="password"
            placeholder="비밀번호를 입력해 주세요."
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
          <button onClick={handleLogin}>로그인</button>

          <div className="login-links">
            <span onClick={() => navigate("/signup")}>회원가입</span>
            <span>|</span>
            <span>아이디 찾기</span>
            <span>|</span>
            <span>비밀번호 찾기</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;
