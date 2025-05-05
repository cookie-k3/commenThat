package com.cookiek.commenthat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReferenceDto {

    private String img;
    private String title;
    private String url;
    private Long views;

}
