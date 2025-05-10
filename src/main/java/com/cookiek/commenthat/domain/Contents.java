package com.cookiek.commenthat.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter @Setter
@Table(name = "contents")
public class Contents {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contents_id")
    private Long id;

    @Column(name = "update_date")
    private LocalDateTime updateDate;

    @Column(name = "video_period")
    private String videoPeriod;

    @Lob
    @Column(name = "topic_rec", columnDefinition = "TEXT")
    private String topicRec;

    private String topic;

    @Lob
    @Column(name = "topic_analysis", columnDefinition = "TEXT")
    private String topicAnalysis;

    @Lob
    @Column(name = "top_view_video", columnDefinition = "TEXT")
    private String topViewVideo;

    @Lob
    @Column(name = "top_positive_video", columnDefinition = "TEXT")
    private String topPositiveVideo;

    @Lob
    @Column(name = "top_negative_video", columnDefinition = "TEXT")
    private String topNegativeVideo;

    @Lob
    @Column(name = "positive_keywords", columnDefinition = "TEXT")
    private String positiveKeywords;

    @Lob
    @Column(name = "top_categories", columnDefinition = "TEXT")
    private String topCategories;

//    @Lob
//    @Column(name = "urls", columnDefinition = "TEXT")
//    private String urls;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "contents", cascade = CascadeType.ALL)
    private List<Reference> references = new ArrayList<>();
}