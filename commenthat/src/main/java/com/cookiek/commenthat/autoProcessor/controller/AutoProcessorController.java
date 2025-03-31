package com.cookiek.commenthat.autoProcessor.controller;

import com.cookiek.commenthat.autoProcessor.service.FetchInitialDataService;
import com.cookiek.commenthat.domain.User;
import com.cookiek.commenthat.service.ChannelInfoService;
import com.cookiek.commenthat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
public class AutoProcessorController {

    private final ChannelInfoService channelInfoService;

    @GetMapping("/fetch-channel-info")
    @Transactional
    public Map<String, String> fetchChannelInfo(@RequestParam Long userId) {
        channelInfoService.fetchInitData(userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "success fetchChannelInfo");
        return response;
    }

}
