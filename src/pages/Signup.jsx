import { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios"; //HTTP 클라언트 라이브러리
import "../components/Signup.css";
import logo from "../assets/logo.png";
import mashBackground from "../assets/mash.png";
import hatImage from "../assets/hat.png";

const Signup = () => {
  const navigate = useNavigate();
  const [userId, setUserId] = useState("");
  const [name, setName] = useState("");
  const [channelName, setChannelName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [nationality, setNationality] = useState("");
  const [gender, setGender] = useState("");
  const [errors, setErrors] = useState({});

  //유효성 검사 함수
  const validate = () => {
    const newErrors = {};

    if (!userId) {
      newErrors.loginId = "아이디를 입력해주세요.";
    } else if (!/^[a-zA-Z0-9]+$/.test(userId)) {
      newErrors.loginId = "아이디는 영문 또는 영문+숫자 조합만 가능합니다.";
    }
    if (!channelName) newErrors.channelName = "채널명을 입력해주세요.";
    if (!name) newErrors.name = "이름을 입력해주세요.";
    if (!email) newErrors.email = "이메일을 입력해주세요.";
    else if (!/\S+@\S+\.\S+/.test(email))
      newErrors.email = "올바른 이메일 형식이 아닙니다.";

    if (!password) newErrors.password = "비밀번호를 입력해주세요.";
    else if (password.length < 8)
      newErrors.password = "비밀번호는 8자 이상이어야 합니다.";

    return newErrors;
  };

  const handleSignup = async () => {
    const validationErrors = validate();
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors); // 프론트 유효성 검사 실패 시 에러 출력
      return;
    }

    try {
      const response = await axios.post(
        "http://localhost:8080/api/users/signup",
        {
          loginId: userId, //프론트에서 받은 userId 값을 loginId라는 키 이름으로 서버에 보냄
          name,
          channelName,
          email,
          password,
          nationality,
          gender: gender === "남자" ? "MALE" : "FEMALE",
        }
      );

      alert(response.data.message); // 백엔드 json에서 message만 추출 ex) 회원가입 완료!
      navigate("/");
    } catch (error) {
      if (
        error.response?.status === 400 &&
        typeof error.response.data === "object"
      ) {
        setErrors(error.response.data); // 백엔드에서 온 유효성 검사 메시지
      } else {
        alert(error.response?.data || "회원가입 실패");
      }
    }
  };

  return (
    <div
      className="signup-container"
      style={{ backgroundImage: `url(${mashBackground})` }}
    >
      {/* 로고 */}
      <div className="logo-container">
        <img
          src={logo}
          alt="CommentHAT Logo"
          className="logo"
          onClick={() => navigate("/")}
        />
      </div>

      <div className="signup-content">
        {/* 좌측 이미지 */}
        <div className="signup-image">
          <img src={hatImage} alt="Hat" />
        </div>

        {/* 우측 회원가입 폼 */}
        <div className="signup-form">
          <input
            type="text"
            placeholder="아이디"
            value={userId}
            onChange={(e) => setUserId(e.target.value)}
          />
          {errors.loginId && <p className="error-text">{errors.loginId}</p>}

          <input
            type="text"
            placeholder="이름"
            value={name}
            onChange={(e) => setName(e.target.value)}
          />
          {errors.name && <p className="error-text">{errors.name}</p>}
          <input
            type="text"
            placeholder="채널명"
            value={channelName}
            onChange={(e) => setChannelName(e.target.value)}
          />
          {errors.channelName && (
            <p className="error-text">{errors.channelName}</p>
          )}
          <input
            type="email"
            placeholder="이메일"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
          {errors.email && <p className="error-text">{errors.email}</p>}

          <input
            type="password"
            placeholder="비밀번호"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
          {errors.password && <p className="error-text">{errors.password}</p>}

          {/* 국적 선택 */}
          <div className="button-group">
            <button
              className={nationality === "내국인" ? "active" : ""}
              onClick={() => setNationality("내국인")}
            >
              내국인
            </button>
            <button
              className={nationality === "외국인" ? "active" : ""}
              onClick={() => setNationality("외국인")}
            >
              외국인
            </button>
          </div>

          {/* 성별 선택 */}
          <div className="button-group">
            <button
              className={gender === "남자" ? "active" : ""}
              onClick={() => setGender("남자")}
            >
              남자
            </button>
            <button
              className={gender === "여자" ? "active" : ""}
              onClick={() => setGender("여자")}
            >
              여자
            </button>
          </div>

          {/* 가입 완료 버튼 */}
          <button className="signup-button" onClick={handleSignup}>
            가입 완료
          </button>
        </div>
      </div>
    </div>
  );
};

export default Signup;
