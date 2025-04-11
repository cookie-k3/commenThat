package com.cookiek.commenthat.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter @Setter
@Table(name = "video_comment")
public class VideoComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "video_comment_id")
    private Long id;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String comment;
    @Column(name = "like_count")
    private Long likeCount;
    @Column(name = "upload_date")
    private LocalDateTime date;

    @Column(name = "is_positive")
    private Integer isPositive; // 0:부정 1:긍정

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "video_id")
    private Video video;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

}
