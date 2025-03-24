package com.cookiek.commenthat.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter @Setter
@Table(name = "topic")
public class Topic {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "topic_id")
    private Long id;

    @Column(name = "create_date")
    private LocalDateTime date;

    @Column(name = "comment_analysis", length = 5000)
    private String commentAnalysis;

    @Column(name = "channel_analysis", length = 5000)
    private String channelAnalysis;

    @Column(name = "topic_rec", length = 5000)
    private String topicRec;

    @Column(name = "related_videos")
    private String relatedVideos;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

}
