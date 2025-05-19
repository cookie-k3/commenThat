import { useNavigate } from "react-router-dom";
import "../components/Home.css";
import "../components/AnalysisCenter.css";
import LeftHeader from "../components/LeftHeader";

// import subImg from "../assets/sub_ex.png";
import totalViewImg from "../assets/totalViewImg.png";
// import viewImg from "../assets/view_ex.png";
import viewImg2 from "../assets/view_ex2.png";
// import sentiImg from "../assets/senti_ex.png";
import sentiImg2 from "../assets/senti_ex2.png";
// import ideaImg from "../assets/idea_ex.png";
import ideaImg2 from "../assets/idea_ex2.png";

const AnalysisCenter = () => {
  const navigate = useNavigate();

  return (
    <div className="home-container">
      <LeftHeader />
      <div className="home-main">
        {/* 상단 헤더 영역 */}
        <div className="home-topbar-wrapper">
          <div className="home-topbar">
            {/* <div className="date-inputs">
              <input type="date" disabled />
              <span> - </span>
              <input type="date" disabled />
            </div>
            <div className="range-buttons">
              <button disabled>1개월</button>
              <button disabled>3개월</button>
              <button disabled>6개월</button>
            </div> */}
            <div className="user-info">
              <span
                onClick={() => navigate("/signup")}
                style={{ cursor: "pointer" }}
              >
                회원가입
              </span>
              <span>|</span>
              <span
                onClick={() => navigate("/login")}
                style={{ cursor: "pointer" }}
              >
                로그인
              </span>
            </div>
          </div>
        </div>

        {/* 콘텐츠 카드 */}
        <div className="dashboard">
          <div className="row">
            <div className="card analysis-card">
              <div className="title-overlay">채널 조회수 분석</div>
              <img src={totalViewImg} alt="구독자 예시" />
              <div className="hover-description">
                <p>
                  채널 전체의 누적 조회수 추이를 분석하여 인기도를 파악합니다.
                </p>
              </div>
            </div>
            <div className="card analysis-card">
              <div className="title-overlay">영상별 조회수 분석</div>
              <img src={viewImg2} alt="조회수 예시" />
              <div className="hover-description">
                <p>영상별 조회수 추이를 분석하여 인기도를 파악합니다.</p>
              </div>
            </div>
          </div>

          <div className="row">
            <div className="card analysis-card">
              <div className="title-overlay">댓글 분석</div>
              <img src={sentiImg2} alt="댓글 예시" />
              <div className="hover-description">
                <p>댓글 범주화와 감정 분석으로 시청자의 반응을 파악합니다.</p>
              </div>
            </div>
            <div className="card analysis-card">
              <div className="title-overlay">콘텐츠 추천</div>
              <img src={ideaImg2} alt="콘텐츠 추천 예시" />
              <div className="hover-description">
                <p>댓글 분석 결과를 기반으로 다음 콘텐츠를 추천합니다.</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AnalysisCenter;
