package com.cookiek.commenthat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ChannelInfoDto {
    private LocalDateTime date;
    private Long subscriber;
    private Long totalViews;
}