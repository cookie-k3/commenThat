import React from "react";
import { useNavigate } from "react-router-dom";
import Header from "../components/Header";
import "../components/About.css";
import firstImg from "../assets/about1.png";
import secondImg from "../assets/about2.png";
import thirdImg from "../assets/about3.png";

const About = () => {
  const navigate = useNavigate();

  return (
    <>
      <Header />
      <div className="about-container">
        {/* 첫 번째 섹션 */}
        <section className="about-section">
          <div className="text-content">
            <button className="tag-button" onClick={() => navigate("/")}>
              commentHAT →
            </button>
            <h1 className="title">빅데이터를 읽다</h1>
            <p className="subtitle">
              사람들의 진짜 마음을 확인하고,
              <br />더 나은 선택을 해보세요.
            </p>
            <div className="feature-boxes">
              <div className="feature-box">
                <span className="emoji">📍</span>
                반짝이는 아이디어, 인사이트를 발견하세요.
              </div>
              <div className="feature-box">
                <span className="emoji">📊</span>
                최고의 빅데이터를 마음껏 누리세요.
              </div>
            </div>
          </div>
          <img src={firstImg} alt="Section 1" className="section-image" />
        </section>

        {/* 두 번째 섹션 */}
        <section className="about-section">
          <div className="text-content">
            <button className="tag-button cloud" onClick={() => navigate("/")}>
              commentHAT →
            </button>
            <h1 className="title">특화된 데이터를 만나다</h1>
            <p className="subtitle">
              비즈니스 환경에 최적화된
              <br />
              빅데이터 분석 솔루션을 제공해요.
            </p>
            <div className="feature-boxes">
              <div className="feature-box">
                <span className="emoji">👤</span>
                전략적이고 합리적인 의사결정을 도와줘요.
              </div>
              <div className="feature-box">
                <span className="emoji">🧠</span>
                크리에이터 맞춤형으로 설계되어 사용자의 시간을 절약해 줘요.
              </div>
            </div>
          </div>
          <img src={secondImg} alt="Section 2" className="section-image" />
        </section>

        {/* 세 번째 섹션 */}
        <section className="about-section">
          <div className="text-content">
            <button className="tag-button data" onClick={() => navigate("/")}>
              commentHAT →
            </button>
            <h1 className="title">데이터와 데이터를 융합하다</h1>
            <p className="subtitle">
              commentHAT에서 제공하는 데이터에
              <br />
              채널 내부 데이터를 더하여 분석할 수 있어요.
            </p>
            <div className="feature-boxes">
              <div className="feature-box">
                <span className="emoji">🔗</span>
                데이터의 융합을 통해 더 큰 시장의 기회를 찾아보세요.
              </div>
              <div className="feature-box">
                <span className="emoji">🛡️</span>
                내부 데이터 유출 없이 안전하게 활용할 수 있어요.
              </div>
            </div>
          </div>
          <img src={thirdImg} alt="Section 3" className="section-image" />
        </section>
      </div>
    </>
  );
};

export default About;
