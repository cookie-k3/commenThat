package com.cookiek.commenthat.autoProcessor.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class VideoCommentDTO {

    private String comment;
    private Long likeCount;
    private LocalDateTime date;

}
