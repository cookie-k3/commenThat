package com.cookiek.commenthat.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter @Setter
@Table(name = "channel_info")
public class ChannelInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "channel_info_id")
    private Long id;

    @Column(name = "update_date")
    private LocalDateTime date;

    @Column(name = "total_views")
    private Long totalViews;

    private Long subscriber;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

}
