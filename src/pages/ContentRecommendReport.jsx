import React, { useEffect, useState } from "react";
import axios from "axios";
import { useAuth } from "../context/AuthContext";
import LeftHeader from "../components/LeftHeader";
import TopBar from "../components/TopBar";
import { useParams } from "react-router-dom";
import "../components/ContentRecommendReport.css";

const ContentRecommendReport = () => {
  const { user } = useAuth();
  const { contentsId } = useParams();
  const [report, setReport] = useState(null);

  useEffect(() => {
    const fetchReport = async () => {
      try {
        const res = await axios.get(
          `http://localhost:8080/api/contents/report?userId=${user.userId}&contentsId=${contentsId}`
        );
        console.log("API 응답:", res.data);
        setReport(res.data);
      } catch (error) {
        console.error("추천 보고서를 불러오지 못했습니다.", error);
      }
    };
    if (user?.userId && contentsId) {
      fetchReport();
    }
  }, [user, contentsId]);

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
                <h3 className="section-title">최근 댓글 피드백 및 추천 결과</h3>
                <p className="section-content">
                  {report.commentAnalysis === "None"
                    ? "관련 분석 결과가 없습니다."
                    : report.commentAnalysis}
                </p>
              </div>

              <div className="report-section">
                <h3 className="section-title">최근 영상 조회수 분석 결과</h3>
                <p className="section-content">
                  {report.channelAnalysis === "None"
                    ? "조회수 분석 결과가 없습니다."
                    : report.channelAnalysis}
                </p>
              </div>

              <div className="report-section">
                <h3 className="section-title">영상 제작 가이드</h3>
                <p className="section-content">{report.topicRec}</p>
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
