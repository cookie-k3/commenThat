package com.cookiek.commenthat.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class CategoryStatWithVideoList {

    private final List<VideoDto> videoDtoList;
    private final Long videoId;
    private final CategoryStatCountDto statCountDto;

}
