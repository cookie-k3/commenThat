package com.cookiek.commenthat.repository;

import com.cookiek.commenthat.domain.VideoMeta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoMetaInterface extends JpaRepository<VideoMeta, Long> {
}
