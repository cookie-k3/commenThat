import React, { useEffect, useState } from "react";
import axios from "axios";
import { useAuth } from "../context/AuthContext";
import LeftHeader from "../components/LeftHeader";
import TopBar from "../components/TopBar";
import { useParams } from "react-router-dom";
import "../components/ContentRecommendReport.css";

const ContentRecommendReport = () => {
  const { user } = useAuth();
  const { index } = useParams();
  const [report, setReport] = useState(null);
  const [videos, setVideos] = useState([]);

  useEffect(() => {
    const fetchReport = async () => {
      try {
        const res = await axios.get(
          `http://localhost:8080/api/contents/report?userId=${user.userId}&index=${index}`
        );
        console.log("API 응답:", res.data);
        setReport(res.data);
      } catch (error) {
        console.error("추천 보고서를 불러오지 못했습니다.", error);
      }
    };
    if (user?.userId && index !== undefined) {
      fetchReport();
    }
  }, [user, index]);

  // 영상 데이터 호출
  useEffect(() => {
    const fetchVideos = async () => {
      try {
        const res = await axios.get(
          `http://localhost:8080/api/contents/topic-urls?userId=${user.userId}`
        );
        console.log("영상 데이터 응답:", res.data);
        // index 값에 맞는 영상 정보 추출
        setVideos(res.data[parseInt(index)]?.references || []); // ✅ referencesDto → references로 맞춤
      } catch (error) {
        console.error("영상 데이터를 불러오지 못했습니다.", error);
      }
    };
    if (user?.userId && index !== undefined) {
      fetchVideos();
    }
  }, [user, index]);

  return (
    <div className="home-container">
      <LeftHeader />
      <div className="home-main">
        <TopBar />
        <div className="report-wrapper">
          {report ? (
            <>
              <h2 className="report-title">{report.topic} 추천 보고서</h2>

              <div className="report-section">
                <h3 className="section-title">
                  댓글 피드백 및 조회수 분석 결과
                </h3>
                <p className="section-content">
                  {report.channelAnalysis === "None"
                    ? "조회수 분석 결과가 없습니다."
                    : report.topicAnalysis}
                </p>
              </div>

              <div className="report-section">
                <h3 className="section-title">영상 제작 가이드</h3>
                <p className="section-content">{report.topicRec}</p>
              </div>

              {/* ✅ 추천 관련 영상 섹션 */}
              <div className="report-section">
                <h3 className="section-title">추천 관련 영상</h3>
                <div className="thumbnail-grid">
                  {videos
                    .sort((a, b) => b.views - a.views)
                    .slice(0, 4)
                    .map((ref, i) => {
                      const cleanUrl = ref.url.replace(/"/g, "");
                      return (
                        <div key={i} className="thumbnail-wrapper">
                          <a
                            href={cleanUrl}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="thumbnail-link"
                          >
                            <img
                              src={ref.img}
                              alt={ref.title}
                              className="thumbnail-img"
                            />
                            <div className="thumbnail-hover-title">
                              {ref.title}
                            </div>
                          </a>
                        </div>
                      );
                    })}
                </div>
              </div>
            </>
          ) : (
            <p style={{ color: "#999", fontSize: "14px" }}>
              추천 보고서를 불러오는 중입니다...
            </p>
          )}
        </div>
      </div>
    </div>
  );
};

export default ContentRecommendReport;
