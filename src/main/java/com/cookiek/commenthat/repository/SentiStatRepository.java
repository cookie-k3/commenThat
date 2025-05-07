package com.cookiek.commenthat.repository;

import com.cookiek.commenthat.domain.SentiStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SentiStatRepository extends JpaRepository<SentiStat, Long> {

    // 특정 영상(videoId)에 대한 긍정/부정 댓글 수 조회 (0번 인덱스: 부정 수, 1번 인덱스: 긍정 수)
    List<SentiStat> findByVideoId(Long videoId);

    /**
     * 긍정 댓글 수 기준으로 유저를 랭킹하는 쿼리
     * - SentiStat 테이블에서 is_positive = 1 (긍정)인 것만 조회
     * - SentiStat -> Video -> User를 JOIN
     * - 같은 유저(userId)끼리 긍정 댓글 수(count)를 합산(SUM)
     * - 긍정 댓글 수 합계 기준으로 내림차순 정렬
     *
     * 결과 컬럼:
     *   [0]: userId
     *   [1]: loginId
     *   [2]: channelImg
     *   [3]: 긍정 댓글 합계(count)
     */
    @Query("""
    SELECT v.user.userId, v.user.loginId, v.user.channelImg, v.user.channelName, SUM(s.count)
    FROM SentiStat s
    JOIN s.video v
    WHERE s.isPositive = 1
    GROUP BY v.user.userId, v.user.loginId, v.user.channelImg, v.user.channelName
    ORDER BY SUM(s.count) DESC
""")
    List<Object[]> findTopUsersByPositiveComments();
}