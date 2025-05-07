package com.cookiek.commenthat.controller;

import com.cookiek.commenthat.dto.UserRankingDto;
import com.cookiek.commenthat.service.ReputationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List; 

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reputation")
public class ReputationController {

    private final ReputationService reputationService;

    @GetMapping("/views")
    public List<UserRankingDto> getTopUsersByViews() {
        return reputationService.getTopUsersByViews();
    }

    @GetMapping("/subscribers")
    public List<UserRankingDto> getTopUsersBySubscribers() {
        return reputationService.getTopUsersBySubscribers();
    }

    @GetMapping("/positive")
    public List<UserRankingDto> getTopUsersByPositiveComments() {
        return reputationService.getTopUsersByPositiveComments();
    }
}
