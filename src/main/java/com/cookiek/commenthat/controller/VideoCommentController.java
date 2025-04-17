package com.cookiek.commenthat.controller;

import com.cookiek.commenthat.dto.*;
import com.cookiek.commenthat.service.CategoryStatService;
import com.cookiek.commenthat.service.SentiService;
import com.cookiek.commenthat.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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


    // 모든 영상의 id와 제목을 반환해야하는지 확인하기!!


    /**
     * 범주화
     * */

    //http://localhost:8080/api/comments/category-chart-init?userId=2
    @GetMapping("/category-chart-init")
    public ResponseEntity<?> getCategoryByUserId(@RequestParam Long userId) {
        try {
            Long videoId = videoService.getRecentVideoIdByUserId(userId);
            if (videoId == null) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error("최근 영상이 없습니다."));
            }

            // 최근 영상에 대한 범주 통계
            CategoryStatCountDto categoryStatCountDto = categoryStatService.getCategoryCountByVideoId(videoId);

            // 유저의 전체 영상 목록
            List<VideoDto> videoDtoList = videoService.getVideoList(userId);

            // 전체 응답 객체 생성
            CategoryStatWithVideoListDto response = new CategoryStatWithVideoListDto(videoDtoList, videoId, categoryStatCountDto);

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("서버 내부 오류 발생"));
        }
    }

    //http://localhost:8080/api/comments/category-chart-videoid?videoId=35
    @GetMapping("/category-chart-videoid")
    public ResponseEntity<CategoryStatWithVideoIdDto> getCategoryByVideoId(@RequestParam Long videoId) {

        CategoryStatCountDto categoryStatCountDto = categoryStatService.getCategoryCountByVideoId(videoId);

        CategoryStatWithVideoIdDto response = new CategoryStatWithVideoIdDto(videoId, categoryStatCountDto);

        return ResponseEntity.ok(response);

    }

    //http://localhost:8080/api/comments/category-comment?videoId=35&categoryId=1
    @GetMapping("/category-comment")
    public ResponseEntity<CategoryCommentsDto> getCategoryComment(@RequestParam Long videoId, @RequestParam Long categoryId) {

        //댓글 리스트
        List<String> comments = categoryStatService.getComments(videoId, categoryId);

        //요약
        String summary = categoryStatService.getSummary(videoId, categoryId);

        //전체 합친 dto(범주화id, 요약, 댓글리스트)
        CategoryCommentsDto response = new CategoryCommentsDto(categoryId, summary, comments);

        return ResponseEntity.ok(response);

    }

    /**
     * 긍부정
     * */

    //http://localhost:8080/api/comments/senti-chart-init?userId=2
    @GetMapping("/senti-chart-init")
    public ResponseEntity<SentiWithVideoListDto> getSentiByUserId(@RequestParam Long userId) {

        //최근 영상 선택
        Long videoId = videoService.getRecentVideoIdByUserId(userId);
        List<Long> negativePositive = sentiService.getSentiCount(videoId);
        List<VideoDto> videoDtoList = videoService.getVideoList(userId);

        SentiWithVideoListDto response = new SentiWithVideoListDto(videoId, negativePositive.get(0), negativePositive.get(1), videoDtoList);

        return ResponseEntity.ok(response);

    }

    @GetMapping("/senti-chart-videoid")
    public ResponseEntity<SentiWithVideoIdDto> getSentiByVideoId(@RequestParam Long videoId) {

        List<Long> negativePositive = sentiService.getSentiCount(videoId);

        SentiWithVideoIdDto response = new SentiWithVideoIdDto(videoId, negativePositive.get(0), negativePositive.get(1));

        return ResponseEntity.ok(response);

    }


}