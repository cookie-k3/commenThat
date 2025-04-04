package com.cookiek.commenthat.autoProcessor.controller;

import com.cookiek.commenthat.autoProcessor.service.FetchChannelInfoService;
import com.cookiek.commenthat.domain.User;
import com.cookiek.commenthat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AutoProcessorController {

    private final UserService userService;
    private final FetchChannelInfoService fetchChannelInfoService;

    @GetMapping("/fetch-channel-info")
    @Transactional
    //    @Scheduled(cron = "0 0 12 * * ?") // 매일 12시에 실행
    public Map<String, String> fetchChannelInfo(@RequestParam Long userId) {
        User user = userService.findUserById(userId);
        String channelId = user.getChannelId();
        fetchChannelInfoService.fetchChannelInfoAsync(channelId, user.getId());

        Map<String, String> response = new HashMap<>();
        response.put("message", "success fetchChannelInfo");
        return response;
    }

}
