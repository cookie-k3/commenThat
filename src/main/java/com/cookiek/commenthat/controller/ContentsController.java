package com.cookiek.commenthat.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.cookiek.commenthat.dto.CategoryCommentsDto;
import com.cookiek.commenthat.dto.ReportDto;
import com.cookiek.commenthat.dto.TopicUrlsDto;
import com.cookiek.commenthat.repository.ContentsRepository;
import com.cookiek.commenthat.service.ContentsService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    //http://localhost:8080/api/contents/report?userId=2&index=0
    @GetMapping("/report")
    public ReportDto getReport(@RequestParam Long userId, @RequestParam int index) {
        ReportDto result = contentsService.getContentsReport(userId);

        ObjectMapper mapper = new ObjectMapper();
        List<String> topics = List.of();
        List<String> topicRecs = List.of();
        List<String> topicAnalyses = List.of();

        try {
            topics = mapper.readValue(result.getTopic(), new TypeReference<List<String>>() {});
            topicRecs = mapper.readValue(result.getTopicRec(), new TypeReference<List<String>>() {});
            topicAnalyses = mapper.readValue(result.getTopicAnalysis(), new TypeReference<List<String>>() {});
        } catch (Exception e) {
            e.printStackTrace();
        }

        String topic = topics.size() > index ? topics.get(index) : null;
        String topicRec = topicRecs.size() > index ? topicRecs.get(index) : null;
        String topicAnalysis = topicAnalyses.size() > index ? topicAnalyses.get(index) : null;

        return new ReportDto(
                null,
                topic,
                topicAnalysis,
                topicRec,
                null, null, null, null, null
        );
    }


    //추천 통계 요약
    //http://localhost:8080/api/contents/summary?userId=2
    @GetMapping("/summary")
    public ResponseEntity<ReportDto> getContentsSummary(@RequestParam Long userId) {

        ReportDto response = contentsService.getContentsSummary(userId);

        return ResponseEntity.ok(response);
    }

}
