package com.cookiek.commenthat.controller;

import com.cookiek.commenthat.dto.ChannelInfoDto;
import com.cookiek.commenthat.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analysis")
public class AnalysisController {
    private final AnalysisService analysisService;

    @GetMapping("/subscriber")
    public ResponseEntity<List<ChannelInfoDto>> getSubscriberTrend(@RequestParam Long userId) {
        return ResponseEntity.ok(analysisService.getSubscriberTrend(userId));
    }
    @GetMapping("/views")
    public ResponseEntity<List<ChannelInfoDto>> getViewTrend(@RequestParam Long userId) {
        return ResponseEntity.ok(analysisService.getTotalViews(userId));
    }
}