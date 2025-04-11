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
@Table(name = "video_meta")
public class VideoMeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "video_meta_id")
    private Long id;

    @Column(name = "update_date")
    private LocalDateTime date;
    private Long views;
    private Long likes;
    private Long subscriber;

    @Column(name = "comment_count")
    private Long commentCount;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "video_id")
    private Video video;

}

