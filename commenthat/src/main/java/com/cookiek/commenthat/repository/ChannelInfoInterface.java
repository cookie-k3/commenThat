package com.cookiek.commenthat.repository;

import com.cookiek.commenthat.domain.ChannelInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelInfoInterface extends JpaRepository<ChannelInfo, Long> {
}
