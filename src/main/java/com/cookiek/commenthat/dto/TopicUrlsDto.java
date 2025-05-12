package com.cookiek.commenthat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TopicUrlsDto {

    private Long contentsId;             // Contents 테이블의 ID
    private String topic;                // 추천 키워드 (예: "도심 속 숨은 맛집 탐방")
    private List<ReferenceDto> references; // 해당 키워드에 연관된 영상 리스트
}
