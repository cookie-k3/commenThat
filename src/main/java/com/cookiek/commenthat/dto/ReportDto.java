package com.cookiek.commenthat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ReportDto {

    private LocalDateTime updateDate;

    private String topic;

    private String channelAnalysis;
    private String commentAnalysis;
    private String topicRec;
}
