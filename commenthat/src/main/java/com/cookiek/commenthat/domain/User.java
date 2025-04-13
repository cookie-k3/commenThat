package com.cookiek.commenthat.domain;

import jakarta.annotation.Nullable;
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
    @Column(name = "user_id", nullable = false)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String email;
    @Column(name = "login_id", nullable = false)
    private String loginId;
    @Column(nullable = false)
    private String password;

    private String gender;

    @Column(name = "channel_name", nullable = false)
    private String channelName;

    @Column(name = "channel_id")
    @Nullable
    private String channelId;

    @Column(nullable = false)
    private String nationality;
//    private String api;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Contents> contents = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ChannelInfo> channelInfos = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Video> videos = new ArrayList<>();

}
