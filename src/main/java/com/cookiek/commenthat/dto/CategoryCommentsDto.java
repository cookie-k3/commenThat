package com.cookiek.commenthat.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class CategoryCommentsDto {

    private final Long categoryId;
    private final String summary;
    private final List<String> comments;

}
