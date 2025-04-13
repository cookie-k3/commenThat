package com.cookiek.commenthat.controller;

import com.cookiek.commenthat.common.ApiResponse;
import com.cookiek.commenthat.dto.CategoryStatCountDto;
import com.cookiek.commenthat.service.CategoryStatService;
import com.cookiek.commenthat.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class VideoCommentController {

    @Autowired
    CategoryStatService categoryStatService;
    @Autowired
    VideoService videoService;


    @GetMapping("/category-chart")
    public ResponseEntity<CategoryStatCountDto> getCategoryByUserId(@RequestParam Long userId) {

        //최근 영상 선택
        Long videoId = videoService.getRecentVideoIdByUserId(userId);

        return ResponseEntity.ok(categoryStatService.getCategoryCountByVideoId(videoId));

    }
}
