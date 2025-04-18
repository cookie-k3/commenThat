// src/components/TopBar.jsx
import React from "react";
import { useAuth } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";
import "../components/Home.css";

const TopBar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout(); // 상태 초기화 + localStorage 제거
    navigate("/"); // 메인 화면으로 이동
  };

  return (
    <div className="home-topbar-wrapper">
      <div className="home-topbar">
        <div className="user-info">
          <span>{user?.loginId}</span>
          <span>|</span>
          <span style={{ cursor: "pointer" }} onClick={handleLogout}>
            로그아웃
          </span>
        </div>
      </div>
    </div>
  );
};

export default TopBar;
// // src/components/TopBar.jsx 날짜 선택 버전 / 주의: 로그아웃 기능 없는 버전임
// import React, { useState } from "react";
// import { useAuth } from "../context/AuthContext";
// import "../components/Home.css";
// import DatePicker from "react-datepicker";
// import "react-datepicker/dist/react-datepicker.css";
// import { addMonths, isAfter } from "date-fns";

// const TopBar = () => {
//   const { user } = useAuth();
//   const today = new Date();
//   const [startDate, setStartDate] = useState(
//     new Date(today.getFullYear(), today.getMonth(), 1)
//   );
//   const [endDate, setEndDate] = useState(addMonths(startDate, 1));

//   // 최대 3개월까지만 허용
//   const validateDateRange = (newStart, newEnd) => {
//     const maxDate = addMonths(newStart, 3);
//     return newEnd >= newStart && !isAfter(newEnd, maxDate);
//   };

//   const handleStartChange = (date) => {
//     if (validateDateRange(date, endDate)) {
//       setStartDate(date);
//     } else {
//       alert("시작일과 종료일은 최대 3개월 이내로만 설정 가능합니다.");
//     }
//   };

//   const handleEndChange = (date) => {
//     if (validateDateRange(startDate, date)) {
//       setEndDate(date);
//     } else {
//       alert("시작일과 종료일은 최대 3개월 이내로만 설정 가능합니다.");
//     }
//   };

//   const setPresetRange = (months) => {
//     setStartDate(today);
//     setEndDate(addMonths(today, months));
//   };

//   return (
//     <div className="home-topbar-wrapper">
//       <div className="home-topbar">
//         {/* 날짜 선택 영역 */}
//         <div className="date-inputs">
//           <DatePicker
//             selected={startDate}
//             onChange={handleStartChange}
//             dateFormat="yyyy-MM-dd"
//             className="datepicker-input"
//           />
//           <span> - </span>
//           <DatePicker
//             selected={endDate}
//             onChange={handleEndChange}
//             dateFormat="yyyy-MM-dd"
//             className="datepicker-input"
//           />
//         </div>

//         {/* 1/3개월 버튼 */}
//         <div className="range-buttons">
//           <button onClick={() => setPresetRange(1)}>1개월</button>
//           <button onClick={() => setPresetRange(3)}>3개월</button>
//         </div>

//         {/* 유저 정보 + 로그아웃 */}
//         <div className="user-info">
//           <span>{user?.loginId}</span>
//           <span>|</span>
//           <span style={{ cursor: "pointer" }}>로그아웃</span>
//         </div>
//       </div>
//     </div>
//   );
// };

// export default TopBar;
