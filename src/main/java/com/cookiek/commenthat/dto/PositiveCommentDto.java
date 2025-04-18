package com.cookiek.commenthat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
public class PositiveCommentDto {

    private String text;
    private Long value;

}
