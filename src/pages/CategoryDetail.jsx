// src/pages/CategoryDetail.jsx
import React, { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import axios from "axios";
import "../components/LeftHeader.css";
import "../components/CategoryDetail.css";
import LeftHeader from "../components/LeftHeader";

const categoryNameMap = {
  1: "즐거움",
  2: "슬픔",
  3: "분노",
  4: "무서움",
  5: "행복",
  6: "응원",
  7: "걱정",
  8: "공감",
  9: "축하",
  10: "질문",
  11: "요청",
  12: "칭찬",
  13: "혐오",
  14: "기타",
};

const CategoryDetail = () => {
  const location = useLocation();
  const params = new URLSearchParams(location.search);
  const videoId = params.get("videoId");
  const categoryId = params.get("categoryId");

  const navigate = useNavigate();

  const [summary, setSummary] = useState("");
  const [comments, setComments] = useState([]);
  const [dateRange, setDateRange] = useState(["2024-10-01", "2024-11-01"]); // 샘플 날짜

  useEffect(() => {
    const fetchDetail = async () => {
      try {
        const res = await axios.get(
          `http://localhost:8080/api/comments/category-comment?videoId=${videoId}&categoryId=${categoryId}`
        );
        setSummary(res.data.summary);
        setComments(res.data.comments);
      } catch (e) {
        console.error("상세 조회 실패", e);
      }
    };

    fetchDetail();
  }, [videoId, categoryId]);

  return (
    <div className="home-container">
      <LeftHeader />
      <div className="home-main detail-main">
        {/* 상단 뒤로가기 버튼, 제목 */}
        <div className="detail-header">
          <button
            onClick={() => navigate(`/category?videoId=${videoId}`)}
            className="back-button"
          >
            ← 뒤로가기
          </button>
          <div className="detail-title">
            <h2>{categoryNameMap[categoryId]} 댓글</h2>
          </div>
        </div>

        {/* 요약 영역 */}
        <div className="summary-box">
          <h4>{categoryNameMap[categoryId]} 댓글 요약</h4>
          <p>{summary}</p>
        </div>

        {/* 댓글 원문 리스트 */}
        <div className="comment-list-box">
          {/* 제목은 박스 바깥에 */}
          <h4 className="comment-list-title">
            {categoryNameMap[categoryId]} 댓글 원본
          </h4>

          {/* 댓글 목록만 스크롤 되게 */}
          <div className="comment-table-wrapper">
            <table className="comment-table">
              <tbody>
                {comments.map((text, idx) => (
                  <tr key={idx}>
                    <td className="comment-count">#{idx + 1}</td>
                    <td className="comment-text">{text}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CategoryDetail;
