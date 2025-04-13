package com.cookiek.commenthat.dto;

import lombok.Getter;

@Getter
public class CategoryStatCountDto {

    private final String joy;
    private final String sadness;
    private final String anger;
    private final String fear;
    private final String happiness;
    private final String cheering;
    private final String concern;
    private final String sympathy;
    private final String congratulations;
    private final String question;
    private final String suggestion;
    private final String praise;
    private final String hate;
    private final String other;

    // 순서대로 매핑되는 생성자
    public CategoryStatCountDto(
            String joy,
            String sadness,
            String anger,
            String fear,
            String happiness,
            String cheering,
            String concern,
            String sympathy,
            String congratulations,
            String question,
            String suggestion,
            String praise,
            String hate,
            String other
    ) {
        this.joy = joy;
        this.sadness = sadness;
        this.anger = anger;
        this.fear = fear;
        this.happiness = happiness;
        this.cheering = cheering;
        this.concern = concern;
        this.sympathy = sympathy;
        this.congratulations = congratulations;
        this.question = question;
        this.suggestion = suggestion;
        this.praise = praise;
        this.hate = hate;
        this.other = other;
    }

}
