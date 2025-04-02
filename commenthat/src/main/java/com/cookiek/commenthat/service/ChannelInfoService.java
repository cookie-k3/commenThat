package com.cookiek.commenthat.service;

import com.cookiek.commenthat.autoProcessor.dto.ChannelInfoDTO;
import com.cookiek.commenthat.autoProcessor.service.FetchInitialDataService;
import com.cookiek.commenthat.domain.ChannelInfo;
import com.cookiek.commenthat.domain.User;
import com.cookiek.commenthat.repository.ChannelInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChannelInfoService {
    private final FetchInitialDataService fetchInitialDataService;
    private final UserService userService;

    @Transactional
//    @Scheduled(cron = "0 0 12 * * ?") // 매일 12시에 실행
    public void fetchInitData(Long userId) {
        User user = userService.findUserById(userId);
        String channelName = user.getChannelName();

        fetchInitialDataService.fetchChannelInfoAsync(channelName, user.getId());
    }


}
