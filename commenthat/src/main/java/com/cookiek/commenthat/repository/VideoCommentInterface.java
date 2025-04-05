package com.cookiek.commenthat.repository;

import com.cookiek.commenthat.domain.VideoComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoCommentInterface extends JpaRepository<VideoComment, Long> {
}
