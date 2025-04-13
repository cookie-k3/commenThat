package com.cookiek.commenthat.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class VideoRepository {

    private EntityManager em;

    public Long getRecentVideoId(Long userId) {
        return em.createQuery("""
            SELECT v.id
            FROM Video v
            WHERE v.user.id = :userId
            ORDER BY v.date DESC
            """, Long.class)
                .setParameter("userId", userId)
                .setMaxResults(1)  // 가장 최근 1개만
                .getSingleResult();
    }

}
