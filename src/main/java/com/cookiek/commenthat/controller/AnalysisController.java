package com.cookiek.commenthat.controller;

import com.cookiek.commenthat.dto.*;
import com.cookiek.commenthat.service.AnalysisService;
import com.cookiek.commenthat.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analysis")
public class AnalysisController {
    private final AnalysisService analysisService;
    private final VideoService videoService; // 추가

    @GetMapping("/views")
    public ResponseEntity<List<ChannelInfoDto>> getViewTrend(@RequestParam Long userId) {
        return ResponseEntity.ok(analysisService.getTotalViews(userId));
    }
    @GetMapping("/video-views")
    public ResponseEntity<List<VideoViewStatDto>> getVideoViewTrend(@RequestParam Long videoId) {
        return ResponseEntity.ok(analysisService.getVideoViewTrend(videoId));
    }
    @GetMapping("/view-chart-init")
    public ResponseEntity<?> getVideoListForViewChart(@RequestParam Long userId) {
        List<VideoDto> videoList = videoService.getVideoList(userId);
        Long videoId = videoList.isEmpty() ? null : videoList.get(0).getVideoId();

        CategoryStatWithVideoListDto response = new CategoryStatWithVideoListDto(
                videoList,
                videoId,
                null // CategoryStatCountDto는 여기선 필요 없음
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

}