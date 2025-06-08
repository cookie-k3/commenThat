package com.cookiek.commenthat.repository;

import com.cookiek.commenthat.domain.VideoMeta;
import com.cookiek.commenthat.dto.UserRankingDto;
import com.cookiek.commenthat.dto.VideoViewStatDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface VideoMetaRepository extends Repository<VideoMeta, Long> {

    @Query("""

            SELECT new com.cookiek.commenthat.dto.UserRankingDto(
        v.user.userId,
        v.user.loginId,
        v.user.channelImg,
        v.user.channelName,  
        SUM(vm.views)
            )
            FROM VideoMeta vm
            JOIN vm.video v
            GROUP BY v.user.userId, v.user.loginId, v.user.channelImg, v.user.channelName
            ORDER BY SUM(vm.views) DESC
    """)
    List<UserRankingDto> findTopUsersByTotalViews();

    @Query("""

            SELECT new com.cookiek.commenthat.dto.UserRankingDto(
        v.user.userId,
        v.user.loginId,
        v.user.channelImg,
        v.user.channelName,  
        SUM(vm.subscriber)
    )
    FROM VideoMeta vm
    JOIN vm.video v
    GROUP BY v.user.userId, v.user.loginId, v.user.channelImg, v.user.channelName
    ORDER BY SUM(vm.subscriber) DESC
    """)
    List<UserRankingDto> findTopUsersBySubscribers();

    // 추가: 영상별 최근 7일 조회수

    @Query("""
SELECT new com.cookiek.commenthat.dto.VideoViewStatDto(
    vm.date,
    vm.views
)
FROM VideoMeta vm
WHERE vm.video.id = :videoId
ORDER BY vm.date DESC
""")
    List<VideoViewStatDto> findRecentViewsByVideoId(Long videoId, Pageable pageable);
}

