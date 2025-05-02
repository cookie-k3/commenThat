package com.cookiek.commenthat.controller;

import com.cookiek.commenthat.dto.CategoryCommentsDto;
import com.cookiek.commenthat.dto.ReportDto;
import com.cookiek.commenthat.dto.TopicUrlsDto;
import com.cookiek.commenthat.service.ContentsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class ContentsController {

    private final ContentsService contentsService;


    // 전체 메인 화면에서 토픽 보이도록 하는 코드
    //http://localhost:8080/api/contents/topic-init?userId=2
    @GetMapping("/topic-init")
    public ResponseEntity<List<String>> getTopicsInit(@RequestParam Long userId) {

        List<String> response = contentsService.getTopics(userId);

        return ResponseEntity.ok(response);

    }

    // 토픽 및 url 보여주는 코드
    //http://localhost:8080/api/contents/topic-urls?userId=2
    @GetMapping("/topic-urls")
    public ResponseEntity<List<TopicUrlsDto>> getTopicUrls(@RequestParam Long userId) {
        List<TopicUrlsDto> response = contentsService.getTopicUrls(userId);

        return ResponseEntity.ok(response);
    }

    //추천 보고서 보여주는 코드
    //http://localhost:8080/api/contents/report?userId=2&contentsId=1
    @GetMapping("/report")
    public ResponseEntity<ReportDto> getReport(@RequestParam Long contentsId) {

        ReportDto response = contentsService.getReport(contentsId);

        return ResponseEntity.ok(response);
    }

}
