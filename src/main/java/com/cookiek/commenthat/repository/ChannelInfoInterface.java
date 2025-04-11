package com.cookiek.commenthat.repository;

import com.cookiek.commenthat.domain.ChannelInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChannelInfoInterface extends JpaRepository<ChannelInfo, Long> {
    List<ChannelInfo> findByUser_UserIdOrderByDateAsc(Long userId);
}
