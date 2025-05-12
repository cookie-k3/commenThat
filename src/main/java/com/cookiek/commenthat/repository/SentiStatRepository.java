package com.cookiek.commenthat.repository;

import com.cookiek.commenthat.domain.SentiStat;
import com.cookiek.commenthat.dto.PositiveRatioDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public interface SentiStatRepository extends JpaRepository<SentiStat, Long> {

    List<SentiStat> findByVideoId(Long videoId);

    @Query(value = """
SELECT
    u.user_id AS userId,
    u.login_id AS loginId,
    u.channel_img AS channelImg,
    u.channel_name AS channelName,
    SUM(CASE WHEN s.is_positive = 1 THEN s.count ELSE 0 END) * 1.0 / SUM(s.count) AS ratio
FROM senti_stat s
JOIN video v ON s.video_id = v.video_id
JOIN user u ON v.user_id = u.user_id
WHERE v.upload_date BETWEEN :startDate AND :endDate
GROUP BY u.user_id, u.login_id, u.channel_img, u.channel_name
HAVING SUM(s.count) > 0
ORDER BY ratio DESC
""", nativeQuery = true)
    List<Object[]> findTopUsersByPositiveRatio(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}