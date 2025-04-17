package com.cookiek.commenthat.service;

import com.cookiek.commenthat.domain.ChannelInfo;
import com.cookiek.commenthat.dto.ChannelInfoDto;
import com.cookiek.commenthat.repository.ChannelInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AnalysisService {

    private final ChannelInfoRepository channelInfoRepository;

    public List<ChannelInfoDto> getSubscriberTrend(Long userId) {
        List<ChannelInfo> channelInfos = channelInfoRepository.findByUserIdOrderByDateAsc(userId);
        return channelInfos.stream()
                .map(info -> new ChannelInfoDto(info.getDate(), info.getSubscriber(), null)) //마지막 인자 totalview 안 넣으면 오류 뜨니까 null

                .collect(Collectors.toList());
    }

    public List<ChannelInfoDto> getTotalViews(Long userId) {
        List<ChannelInfo> channelInfos = channelInfoRepository.findByUserIdOrderByDateAsc(userId);
        return channelInfos.stream()
                .map(info -> new ChannelInfoDto(info.getDate(), null, info.getTotalViews())) // subscriber는 null
                .collect(Collectors.toList());
    }


}