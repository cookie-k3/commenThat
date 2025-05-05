package com.cookiek.commenthat.autoProcessor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@AllArgsConstructor
@Getter @Setter
public class TopicUrlsDto {

    private Long contentsId;
    private String topic;
    private List<ReferenceDto> referenceDtos;
}
