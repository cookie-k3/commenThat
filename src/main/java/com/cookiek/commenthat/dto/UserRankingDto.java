package com.cookiek.commenthat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserRankingDto {
    private Long userId;
    private String loginId;
    private String channelImg;
    private String channelName;
    private Long total;

}