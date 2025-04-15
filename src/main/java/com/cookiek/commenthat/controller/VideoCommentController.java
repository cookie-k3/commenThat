package com.cookiek.commenthat.controller;

import com.cookiek.commenthat.dto.CategoryCommentsDto;
import com.cookiek.commenthat.dto.CategoryStatCountDto;
import com.cookiek.commenthat.dto.CategoryStatWithVideoIdDto;
import com.cookiek.commenthat.service.CategoryStatService;
import com.cookiek.commenthat.service.VideoService;
import lombok.RequiredArgsConstructor;
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


    @GetMapping("/category-chart-init")
    public ResponseEntity<CategoryStatWithVideoIdDto> getCategoryByUserId(@RequestParam Long userId) {

        //최근 영상 선택
        Long videoId = videoService.getRecentVideoIdByUserId(userId);
        CategoryStatCountDto categoryStatCountDto = categoryStatService.getCategoryCountByVideoId(videoId);

        CategoryStatWithVideoIdDto response = new CategoryStatWithVideoIdDto(videoId, categoryStatCountDto);

        return ResponseEntity.ok(response);

    }

    @GetMapping("/category-chart-videoid")
    public ResponseEntity<CategoryStatWithVideoIdDto> getCategoryByVideoId(@RequestParam Long videoId) {

        CategoryStatCountDto categoryStatCountDto = categoryStatService.getCategoryCountByVideoId(videoId);

        CategoryStatWithVideoIdDto response = new CategoryStatWithVideoIdDto(videoId, categoryStatCountDto);

        return ResponseEntity.ok(response);

    }

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


}
