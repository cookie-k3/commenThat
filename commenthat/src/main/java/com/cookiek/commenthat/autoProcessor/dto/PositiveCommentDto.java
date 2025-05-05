package com.cookiek.commenthat.autoProcessor.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PositiveCommentDto {

    private String text;
    private Long value;

}
