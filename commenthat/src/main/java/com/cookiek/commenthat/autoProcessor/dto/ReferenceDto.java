package com.cookiek.commenthat.autoProcessor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter@Setter
@Data
public class ReferenceDto {

    private String title;
    private String url;
    private String img;
    private Long views;

}
