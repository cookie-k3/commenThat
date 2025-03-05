import "./App.css";
import Main from "./pages/Main";
import Home from "./pages/Home";
import Notfound from "./pages/Notfound";
import { Routes, Route } from "react-router-dom";

function App() {
  return (
    <Routes>
      <Route path="/" element={<Main />} />
      <Route path="/home" element={<Home />} />
      <Route path="/about" element={<div>소개 페이지</div>} />
      <Route path="/features" element={<div>기능 페이지</div>} />
      <Route path="/analysis" element={<div>분석 페이지</div>} />
      <Route path="/signup" element={<div>회원가입 페이지</div>} />
      <Route path="/login" element={<div>로그인 페이지</div>} />
      <Route path="*" element={<Notfound />} />
    </Routes>
  );
}

export default App;
