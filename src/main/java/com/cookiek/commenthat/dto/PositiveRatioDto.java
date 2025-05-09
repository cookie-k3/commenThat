package com.cookiek.commenthat.dto;

import java.math.BigDecimal;

public class PositiveRatioDto {
    private Long userId;
    private String loginId;
    private String channelImg;
    private String channelName;
    private double ratio;

    public PositiveRatioDto(Object[] row) {
        this.userId = ((Number) row[0]).longValue();
        this.loginId = (String) row[1];
        this.channelImg = (String) row[2];
        this.channelName = (String) row[3];
        this.ratio = ((BigDecimal) row[4]).doubleValue();  // ← 핵심 변환
    }

    // getter들 추가해도 되고, lombok 써도 됨 (@Getter)
}