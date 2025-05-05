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
    private LocalDate videoPeriod;

    @Lob
    @Column(name = "comment_analysis", columnDefinition = "TEXT")
    private String commentAnalysis;

    @Lob
    @Column(name = "channel_analysis", columnDefinition = "TEXT")
    private String channelAnalysis;

    @Lob
    @Column(name = "topic_rec", columnDefinition = "TEXT")
    private String topicRec;

    private String topic;

//    @Lob
//    @Column(name = "urls", columnDefinition = "TEXT")
//    private String urls;

    @OneToMany(mappedBy = "contents", cascade = CascadeType.ALL)
    private List<Reference> references = new ArrayList<>();

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

}
