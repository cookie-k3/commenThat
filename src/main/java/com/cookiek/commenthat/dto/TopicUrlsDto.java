package com.cookiek.commenthat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TopicUrlsDto {

    private Long contentsId;
    private String topic;
    private List<ReferenceDto> referenceDtos;
}
