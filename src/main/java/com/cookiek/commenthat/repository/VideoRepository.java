package com.cookiek.commenthat.repository;

import com.cookiek.commenthat.dto.VideoDto;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class VideoRepository {

    private final EntityManager em;

    public Long getRecentVideoId(Long userId) {
        return em.createQuery("""
            SELECT v.id
            FROM Video v
            WHERE v.user.id = :userId
            ORDER BY v.date DESC
            """, Long.class)
                .setParameter("userId", userId)
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst()
                .orElse(null);
    }

    public Long getMostRecentAnalyzedVideoId(Long userId) {
        return em.createQuery("""
            SELECT v.id
            FROM Video v
            WHERE v.user.id = :userId
            AND EXISTS (
                SELECT 1 FROM CategoryStat cs WHERE cs.video.id = v.id
            )
            AND EXISTS (
                SELECT 1 FROM SentiStat ss WHERE ss.video.id = v.id
            )
            ORDER BY v.date DESC
            """, Long.class)
                .setParameter("userId", userId)
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst()
                .orElse(null);
    }

    public List<VideoDto> getVideoList(Long userId) {
        return em.createQuery("""
            SELECT new com.cookiek.commenthat.dto.VideoDto(v.id, v.title)
            FROM Video v
            WHERE v.user.id = :userId
            ORDER BY v.date DESC
            """, VideoDto.class)
                .setParameter("userId", userId)
                .getResultList();
    }
}