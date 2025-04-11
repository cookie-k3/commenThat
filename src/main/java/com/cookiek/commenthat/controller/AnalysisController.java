package com.cookiek.commenthat.controller;


import com.cookiek.commenthat.domain.ChannelInfo;
import com.cookiek.commenthat.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analysis")
public class AnalysisController {
    private final AnalysisService analysisService;

    @GetMapping("/subscriber")
    public ResponseEntity<List<ChannelInfo>> getSubscriberTrend(@RequestParam Long userId) {
        return ResponseEntity.ok(analysisService.getSubscriberTrend(userId));
    }
}
