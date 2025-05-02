package com.cookiek.commenthat.repository;

import com.cookiek.commenthat.domain.VideoMeta;
import com.cookiek.commenthat.dto.UserRankingDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface VideoMetaRepository extends Repository<VideoMeta, Long> {

    @Query("""
        SELECT new com.cookiek.commenthat.dto.UserRankingDto(
            v.user.userId, v.user.loginId, v.user.channelImg, SUM(vm.views)
        )
        FROM VideoMeta vm
        JOIN vm.video v
        GROUP BY v.user.userId, v.user.loginId, v.user.channelImg
        ORDER BY SUM(vm.views) DESC
        """)
    List<UserRankingDto> findTopUsersByTotalViews();

    @Query("""
        SELECT new com.cookiek.commenthat.dto.UserRankingDto(
            v.user.userId, v.user.loginId, v.user.channelImg, SUM(vm.subscriber)
        )
        FROM VideoMeta vm
        JOIN vm.video v
        GROUP BY v.user.userId, v.user.loginId, v.user.channelImg
        ORDER BY SUM(vm.subscriber) DESC
        """)
    List<UserRankingDto> findTopUsersBySubscribers();
}