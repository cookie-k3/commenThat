package com.cookiek.commenthat.domain;

import com.cookiek.commenthat.service.UserService;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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

    //==연관관계 메서드==//
    public void setUser(User user) {
        this.user = user;
        user.getChannelInfos().add(this);
    }

    //==생성 메서드==//
    public static ChannelInfo createChannelInfo(User user, Long totalViews, Long subscriber) {
        ChannelInfo channelInfo = new ChannelInfo();
        channelInfo.setUser(user);
        channelInfo.setDate(LocalDateTime.now());
        channelInfo.setTotalViews(totalViews);
        channelInfo.setSubscriber(subscriber);
        return channelInfo;
    }

    public static ChannelInfo createChannelInfoWithoutUser(Long userId, Long subscriber, Long totalViews) {
        ChannelInfo channelInfo = new ChannelInfo();
        channelInfo.setDate(LocalDateTime.now());
        channelInfo.setTotalViews(totalViews);
        channelInfo.setSubscriber(subscriber);
        return channelInfo;
    }

}
