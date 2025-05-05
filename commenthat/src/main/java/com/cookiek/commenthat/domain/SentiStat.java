package com.cookiek.commenthat.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name = "senti_stat")
public class SentiStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "senti_stat_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id")
    private Video video;

    @Column(name = "is_positive")
    private Integer isPositive; //  0 = 부정, 1 = 긍정

    private Long count; // 댓글 개수

    @Lob
    @Column(name = "keywords", columnDefinition = "TEXT")
    private String keywords; // 긍정 키워드를 JSON 문자열로 저장 (예: ["여행", "맛집", "행복"])
}