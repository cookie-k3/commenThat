package com.cookiek.commenthat.dto;

import lombok.Getter;
@Getter
public class CategoryStatCountDto {

    private final String joy;
    private final String supportive;
    private final String suggestion;
    private final String hate;
    private final String question;
    private final String praise;
    private final String sympathy;
    private final String congratulations;
    private final String concern;
    private final String other;

    // 순서대로 매핑되는 생성자
    public CategoryStatCountDto(
            String joy,
            String supportive,
            String suggestion,
            String hate,
            String question,
            String praise,
            String sympathy,
            String congratulations,
            String concern,
            String other
    ) {
        this.joy = joy;
        this.supportive = supportive;
        this.suggestion = suggestion;
        this.hate = hate;
        this.question = question;
        this.praise = praise;
        this.sympathy = sympathy;
        this.congratulations = congratulations;
        this.concern = concern;
        this.other = other;
    }
}