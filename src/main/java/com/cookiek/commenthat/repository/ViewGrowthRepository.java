package com.cookiek.commenthat.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ViewGrowthRepository extends Repository<com.cookiek.commenthat.domain.VideoMeta, Long> {

    @Query(value = """

            SELECT 
    recent.user_id,
    u.login_id,
    u.channel_img,
    u.channel_name,
    ((recent.total_views - old.total_views) * 1.0 / NULLIF(old.total_views, 0)) AS growth_rate
FROM (
    SELECT user_id, MAX(update_date) AS recent_date
    FROM channel_info
    WHERE update_date >= CURDATE() - INTERVAL 3 DAY
    GROUP BY user_id
) r
JOIN channel_info recent ON r.user_id = recent.user_id AND r.recent_date = recent.update_date
JOIN (
    SELECT user_id, MIN(update_date) AS old_date
    FROM channel_info
    WHERE update_date >= CURDATE() - INTERVAL 3 DAY
    GROUP BY user_id
) o
ON r.user_id = o.user_id
JOIN channel_info old ON o.user_id = old.user_id AND o.old_date = old.update_date
JOIN user u ON recent.user_id = u.user_id
ORDER BY growth_rate DESC
""", nativeQuery = true)
    List<Object[]> findTopUsersByViewGrowthRateLast3Days();
    }