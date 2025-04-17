package com.cookiek.commenthat.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SentiWithVideoIdDto {

    private final Long videoId;
    private final Long negativeCount;
    private final Long positiveCount;

}
