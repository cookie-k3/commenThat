package com.cookiek.commenthat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
public class VideoViewStatDto {

    private String date;
    private Long views;

    // LocalDateTime을 받아 String으로 변환
    public VideoViewStatDto(java.time.LocalDateTime date, Long views) {
        this.date = date.toLocalDate().toString(); // 또는 원하는 형식으로 포맷
        this.views = views;
    }
}