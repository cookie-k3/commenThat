package com.cookiek.commenthat.service;

import com.cookiek.commenthat.domain.ChannelInfo;
import com.cookiek.commenthat.repository.ChannelInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChannelInfoPersistenceService {
    private final ChannelInfoRepository channelInfoRepository;

    @Transactional
    public void save(ChannelInfo channelInfo) {
        channelInfoRepository.save(channelInfo);
    }
}
