package com.cookiek.commenthat.autoProcessor.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class VideoMetaDTO {
    private Long views;
    private Long likes;
    private Long commentCount;
}
