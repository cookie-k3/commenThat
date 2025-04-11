package com.cookiek.commenthat.domain;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId; // DB용 PK

    @Column(name = "login_id", unique = true, nullable = false)
    private String loginId;    // 사용자가 입력한 "아이디" 🔐

    @Column(nullable = false)
    private String name;        // 🔐 AES-256 암호화

    @Column(name = "channel_name", nullable = false, unique = true)
    private String channelName; // 유튜브 채널명

    @Column(nullable = false)
    private String email;       // 🔐 AES-256 암호화

    @Column(nullable = false)
    private String password;    // 🔐 SHA-256 해시

    @Column(nullable = false)
    private String nationality;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "channel_id")
    @Nullable
    private String channelId;
//  private String api;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Contents> contents = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ChannelInfo> channelInfos = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Video> videos = new ArrayList<>();
}