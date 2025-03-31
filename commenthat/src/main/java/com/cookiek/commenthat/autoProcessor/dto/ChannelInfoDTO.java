package com.cookiek.commenthat.autoProcessor.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ChannelInfoDTO {

    private final Long subscriberCount;
    private final Long viewCount;

}
