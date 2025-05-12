package com.cookiek.commenthat.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface UploadRepository extends Repository<com.cookiek.commenthat.domain.Video, Long> {

    @Query(value = """
    SELECT
        u.user_id AS userId,
        u.login_id AS loginId,
        u.channel_img AS channelImg,
        u.channel_name AS channelName,
        COUNT(v.video_id) AS total
    FROM video v
    JOIN user u ON v.user_id = u.user_id
    WHERE v.upload_date BETWEEN :startDate AND :endDate
    GROUP BY u.user_id, u.login_id, u.channel_img, u.channel_name
    ORDER BY total DESC
    """, nativeQuery = true)
    List<Object[]> findTopUsersByUploadCountNative(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query(value = """
    SELECT *
    FROM video
    WHERE user_id = :userId
      AND upload_date BETWEEN :startDate AND :endDate
    ORDER BY upload_date
    """, nativeQuery = true)
    List<com.cookiek.commenthat.domain.Video> findVideosByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}