package com.cookiek.commenthat.repository;

import com.cookiek.commenthat.domain.VideoComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface VideoCommentInterface extends JpaRepository<VideoComment, Long> {

    @Query("""
   SELECT MAX(vc.date)
   FROM VideoComment vc
   WHERE vc.video.id = :videoId
""")
    LocalDateTime findLatestCommentDate(Long videoId);

}
