package com.cookiek.commenthat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor  // 기본 생성자 생성
public class UserRankingDto {
    private Long userId;
    private String loginId;
    private String channelImg;
    private String channelName;
    private BigDecimal total;

    public UserRankingDto(Long userId, String loginId, String channelImg, String channelName, Long totalValue) {
        this.userId = userId;
        this.loginId = loginId;
        this.channelImg = channelImg;
        this.channelName = channelName;
        this.total = BigDecimal.valueOf(totalValue);  // Long → BigDecimal 변환
    }

}