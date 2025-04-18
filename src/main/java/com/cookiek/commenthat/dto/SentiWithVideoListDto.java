package com.cookiek.commenthat.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class SentiWithVideoListDto {

    private final Long videoId;
    private final Long negativeCount;
    private final Long positiveCount;
    private final List<VideoDto>  videoDtoList;
    private final List<PositiveCommentDto> positiveCommentDtos;

}
