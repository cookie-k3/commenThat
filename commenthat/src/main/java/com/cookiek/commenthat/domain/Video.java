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
@Table(name = "video")
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "video_id")
    private Long id;

    private String title;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "upload_date")
    private LocalDateTime date;

    private String thumbnail;

    @Column(name = "video_youtube_id")
    private String videoYoutubeId;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "video")
    private List<VideoMeta> videoMetas = new ArrayList<>();

    @OneToMany(mappedBy = "video")
    private List<VideoComment> videoComments = new ArrayList<>();

    @OneToMany(mappedBy = "video")
    private List<CategoryStat> categoryStats = new ArrayList<>();
}
