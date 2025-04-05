package com.cookiek.commenthat.repository;

import com.cookiek.commenthat.domain.Video;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoInterface extends JpaRepository<Video, Long> {
}
