package com.cookiek.commenthat.controller;

import com.cookiek.commenthat.dto.*;
import com.cookiek.commenthat.service.CategoryStatService;
import com.cookiek.commenthat.service.SentiService;
import com.cookiek.commenthat.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class VideoCommentController {

    @Autowired
    CategoryStatService categoryStatService;
    @Autowired
    VideoService videoService;
    @Autowired
    SentiService sentiService;

    @GetMapping("/category-chart-init")
    public ResponseEntity<?> getCategoryByUserId(@RequestParam Long userId) {
        try {
            Long videoId = videoService.getMostRecentAnalyzedVideoId(userId);
            if (videoId == null) {
                return ResponseEntity.status(404).body(ApiResponse.error("분석된 영상이 없습니다."));
            }

            CategoryStatCountDto categoryStatCountDto = categoryStatService.getCategoryCountByVideoId(videoId);
            List<VideoDto> videoDtoList = videoService.getVideoList(userId);
            CategoryStatWithVideoListDto response = new CategoryStatWithVideoListDto(videoDtoList, videoId, categoryStatCountDto);

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(ApiResponse.error("서버 내부 오류 발생"));
        }
    }

    @GetMapping("/category-chart-videoid")
    public ResponseEntity<CategoryStatWithVideoIdDto> getCategoryByVideoId(@RequestParam Long videoId) {
        CategoryStatCountDto categoryStatCountDto = categoryStatService.getCategoryCountByVideoId(videoId);
        CategoryStatWithVideoIdDto response = new CategoryStatWithVideoIdDto(videoId, categoryStatCountDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category-comment")
    public ResponseEntity<CategoryCommentsDto> getCategoryComment(@RequestParam Long videoId, @RequestParam Long categoryId) {
        List<String> comments = categoryStatService.getComments(videoId, categoryId);
        String summary = categoryStatService.getSummary(videoId, categoryId);
        CategoryCommentsDto response = new CategoryCommentsDto(categoryId, summary, comments);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/senti-chart-init")
    public ResponseEntity<SentiWithVideoListDto> getSentiByUserId(@RequestParam Long userId) {
        Long videoId = videoService.getMostRecentAnalyzedVideoId(userId);
        List<VideoDto> videoDtoList = videoService.getVideoList(userId);

        if (videoId == null) {
            return ResponseEntity.ok(new SentiWithVideoListDto(null, 0L, 0L, videoDtoList, List.of()));
        }

        List<Long> negativePositive = sentiService.getSentiCount(videoId);
        List<PositiveCommentDto> positiveCommentDtos = sentiService.getPositiveWord(videoId);

        if (negativePositive == null || positiveCommentDtos == null) {
            return ResponseEntity.ok(new SentiWithVideoListDto(videoId, 0L, 0L, videoDtoList, List.of()));
        }

        SentiWithVideoListDto response = new SentiWithVideoListDto(
                videoId,
                negativePositive.get(0),
                negativePositive.get(1),
                videoDtoList,
                positiveCommentDtos
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/senti-chart-videoid")
    public ResponseEntity<SentiWithVideoIdDto> getSentiByVideoId(@RequestParam Long videoId) {
        List<Long> negativePositive = sentiService.getSentiCount(videoId);
        List<PositiveCommentDto> positiveCommentDtos = sentiService.getPositiveWord(videoId);

        if (negativePositive == null || positiveCommentDtos == null) {
            return ResponseEntity.ok(new SentiWithVideoIdDto(videoId, 0L, 0L, List.of()));
        }

        SentiWithVideoIdDto response = new SentiWithVideoIdDto(
                videoId,
                negativePositive.get(0),
                negativePositive.get(1),
                positiveCommentDtos
        );

        return ResponseEntity.ok(response);
    }
}