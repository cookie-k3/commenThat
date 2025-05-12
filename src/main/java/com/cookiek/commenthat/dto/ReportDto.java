package com.cookiek.commenthat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ReportDto {

    private LocalDateTime updateDate;

    private String topic;

    private String topicAnalysis;
    private String topicRec;
    private String topViewVideo;
    private String topPositiveVideo;
    private String topNegativeVideo;
    private String topPositiveKeywords;
    private String topCategories;

}
