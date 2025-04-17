package com.cookiek.commenthat.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CategoryStatWithVideoIdDto {

    private final Long videoId;
    private final CategoryStatCountDto statCountDto;
}