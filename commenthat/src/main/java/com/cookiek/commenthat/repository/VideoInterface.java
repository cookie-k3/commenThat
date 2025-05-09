package com.cookiek.commenthat.repository;

import com.cookiek.commenthat.domain.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface VideoInterface extends JpaRepository<Video, Long> {

    Video findByVideoYoutubeId(String videoYoutubeId);  // YouTube ID로 Video 조회

    List<Video> findByUserIdAndDateAfter(Long userId, LocalDateTime date);

//    // 또는 JPQL 직접 선언
//    @Query("SELECT v FROM Video v WHERE v.user.id = :userId AND v.date >= :date")
//    List<Video> findRecentVideosByUserId(@Param("userId") Long userId, @Param("date") LocalDateTime date);

    List<Video> findAllByUserId(Long userId);
    List<Video> findAllByDateAfter(LocalDateTime date);
    List<Video> findAllByUserIdGreaterThan(Long userId);
    boolean existsByVideoYoutubeId(String videoYoutubeId);

    @Query("""
    select v.id
    from Video v
    """)
    List<Long> findVideoIds();

    @Query("""
        select v.id
        from   Video v
        where  v.user.id = :userId
    """)
    List<Long> findVideoIdsByUserId(@Param("userId") Long userId);




}
