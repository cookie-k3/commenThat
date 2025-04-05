package com.cookiek.commenthat.autoProcessor.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class VideoDTO {

    private String title;
    private String description;
    private LocalDateTime uploadDate;
    private String thumbnail;
    private String videoYoutubeId;

}
