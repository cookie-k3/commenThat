package com.cookiek.commenthat.service;

import com.cookiek.commenthat.domain.ChannelInfo;
import com.cookiek.commenthat.repository.ChannelInfoInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AnalysisService {
    private final ChannelInfoInterface channelInfoInterface;

    public List<ChannelInfo> getSubscriberTrend(Long userId) {
        return channelInfoInterface.findByUser_UserIdOrderByDateAsc(userId);
    }
}
