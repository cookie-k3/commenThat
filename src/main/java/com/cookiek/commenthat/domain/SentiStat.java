//package com.cookiek.commenthat.domain;
//
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.Setter;
//
//import static jakarta.persistence.FetchType.LAZY;
//
//@Entity
//@Getter @Setter
//@Table(name = "senti_stat")
//public class SentiStat {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "senti_stat_id")
//    private Long id;
//
//    private Long count;
//
//    @Lob
//    @Column(name = "summary", columnDefinition = "TEXT")
//    private String summary;
//
//    @Column(name = "is_positive")
//    private Integer isPositive; // 0:부정 1:긍정
//
//    @ManyToOne(fetch = LAZY)
//    @JoinColumn(name = "video_id")
//    private Video video;
//
//}
