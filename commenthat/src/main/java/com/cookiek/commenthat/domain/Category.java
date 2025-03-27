package com.cookiek.commenthat.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@Table(name = "category")
public class Category {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @Column(name = "category_type")
    private String categoryType;

    @OneToMany(mappedBy = "category")
    private List<VideoComment> videoComments = new ArrayList<>();

    @OneToMany(mappedBy = "category")
    private List<CategoryStat> categoryStats = new ArrayList<>();

}
