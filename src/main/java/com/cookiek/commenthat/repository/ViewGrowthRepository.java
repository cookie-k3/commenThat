package com.cookiek.commenthat.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ViewGrowthRepository extends Repository<com.cookiek.commenthat.domain.VideoMeta, Long> {

    @Query(value = """
        SELECT
            u.user_id AS userId,
            u.login_id AS loginId,
            u.channel_img AS channelImg,
            u.channel_name AS channelName,
            SUM(CASE WHEN vm.update_date = :endDate THEN vm.views ELSE 0 END) -
            SUM(CASE WHEN vm.update_date = :startDate THEN vm.views ELSE 0 END) AS total
        FROM video_meta vm
        JOIN video v ON vm.video_id = v.video_id
        JOIN user u ON v.user_id = u.user_id
        WHERE vm.update_date BETWEEN :startDate AND :endDate
        GROUP BY u.user_id, u.login_id, u.channel_img, u.channel_name
        ORDER BY total DESC
        """, nativeQuery = true)
    List<Object[]> findTopUsersByViewGrowthRateNative(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}