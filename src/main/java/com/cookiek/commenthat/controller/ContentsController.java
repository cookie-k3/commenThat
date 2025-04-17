package com.cookiek.commenthat.controller;

import com.cookiek.commenthat.dto.CategoryCommentsDto;
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


    //http://localhost:8080/api/contents/topic-init?userId=2
    @GetMapping("/topic-init")
    public ResponseEntity<List<String>> getTopicsInit(@RequestParam Long userId) {

        List<String> response = contentsService.getTopics(userId);

        return ResponseEntity.ok(response);

    }

}
