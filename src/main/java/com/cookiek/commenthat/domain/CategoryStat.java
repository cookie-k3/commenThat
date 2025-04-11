package com.cookiek.commenthat.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter @Setter
@Table(name = "category_stat")
public class CategoryStat {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_stat_id")
    private Long id;

    private Long count;

    @Lob
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "video_id")
    private Video video;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

}
