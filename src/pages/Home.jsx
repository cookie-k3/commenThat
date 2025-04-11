import { useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useEffect, useState, useRef } from "react";
import LeftHeader from "../components/LeftHeader";
import "../components/Home.css";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";
import { addMonths, isAfter } from "date-fns";

const Home = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuth();
  const [checkedLogin, setCheckedLogin] = useState(false);
  const hasPromptedRef = useRef(false);

  const [startDate, setStartDate] = useState(new Date());
  const [endDate, setEndDate] = useState(addMonths(new Date(), 1));

  const isFromLogout = location.state?.fromLogout;

  const handleLogout = () => {
    const confirmLogout = window.confirm("로그아웃하시겠습니까?");
    if (confirmLogout) {
      logout();
      navigate("/", { state: { fromLogout: true } });
    }
  };

  const validateDateRange = (newStart, newEnd) => {
    const max6 = addMonths(newStart, 6);
    const duration = newEnd.getTime() - newStart.getTime();
    return duration > 0 && !isAfter(newEnd, max6);
  };

  const handleStartDateChange = (date) => {
    if (validateDateRange(date, endDate)) {
      setStartDate(date);
    } else {
      alert("시작일과 종료일은 최대 6개월 이내로만 설정할 수 있습니다.");
    }
  };

  const handleEndDateChange = (date) => {
    if (validateDateRange(startDate, date)) {
      setEndDate(date);
    } else {
      alert("시작일과 종료일은 최대 6개월 이내로만 설정할 수 있습니다.");
    }
  };

  const setPresetRange = (months) => {
    const now = new Date();
    setStartDate(now);
    setEndDate(addMonths(now, months));
  };

  useEffect(() => {
    if (isFromLogout) return;
    if (!user && !hasPromptedRef.current) {
      hasPromptedRef.current = true;
      const goLogin = window.confirm(
        "로그인이 필요한 서비스입니다. 로그인 하시겠습니까?"
      );
      if (goLogin) navigate("/login");
      else navigate("/");
    } else if (user && !checkedLogin) {
      setCheckedLogin(true);
    }
  }, [user, checkedLogin, navigate, isFromLogout]);

  if (!user || !checkedLogin) return null;

  return (
    <div className="home-container">
      <LeftHeader />
      <div className="home-main">
        <div className="home-topbar-wrapper">
          <div className="home-topbar">
            <div className="date-range-group">
              {/* 날짜 입력창 */}
              <div className="date-inputs">
                <div className="date-label-group">
                  <span className="date-label">시작일:</span>
                  <DatePicker
                    selected={startDate}
                    onChange={handleStartDateChange}
                    dateFormat="yyyy-MM-dd"
                    className="datepicker-input"
                  />
                </div>
                <span style={{ fontFamily: "GmarketLight", margin: "0 8px" }}>
                  -
                </span>
                <div className="date-label-group">
                  <span className="date-label">종료일:</span>
                  <DatePicker
                    selected={endDate}
                    onChange={handleEndDateChange}
                    dateFormat="yyyy-MM-dd"
                    className="datepicker-input"
                  />
                </div>
              </div>

              {/* 기간 선택 버튼 */}
              <div className="range-buttons">
                <button onClick={() => setPresetRange(1)}>1개월</button>
                <button onClick={() => setPresetRange(3)}>3개월</button>
                <button onClick={() => setPresetRange(6)}>6개월</button>
              </div>
            </div>

            {/* 사용자 정보 + 로그아웃 */}
            <div className="user-info">
              {user.loginId}
              <button className="logout-inline" onClick={handleLogout}>
                Logout
              </button>
            </div>
          </div>
        </div>

        {/* 콘텐츠 카드 영역 */}
        <div className="dashboard">
          <div className="row">
            <div
              className="card clickable"
              onClick={() => navigate("/subscribers")}
            >
              구독자 수 분석
            </div>
            <div className="card clickable" onClick={() => navigate("/views")}>
              조회수 분석
            </div>
          </div>
          <div className="row">
            <div
              className="card clickable"
              onClick={() => navigate("/category")}
            >
              댓글 분석
            </div>
            <div
              className="card clickable"
              onClick={() => navigate("/recommend")}
            >
              콘텐츠 추천
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Home;
