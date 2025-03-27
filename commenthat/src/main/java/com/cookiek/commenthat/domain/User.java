package com.cookiek.commenthat.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@Table(name = "user")
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;
    private String name;
    private String email;
    private String password;
//    private String api;

    @OneToMany(mappedBy = "user")
    private List<Contents> contents = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<ChannelInfo> channelInfos = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Video> videos = new ArrayList<>();
}
