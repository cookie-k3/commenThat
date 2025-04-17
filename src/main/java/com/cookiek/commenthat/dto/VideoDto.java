package com.cookiek.commenthat.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class VideoDto {

    private final Long videoId;
    private final String videoTitle;

}