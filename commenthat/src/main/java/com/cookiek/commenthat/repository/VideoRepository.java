package com.cookiek.commenthat.repository;

import com.cookiek.commenthat.domain.Video;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class VideoRepository {

    private final EntityManager em;

//    public List<Video> findVideosByUserIdGreaterThan(Long userId) {
//        return em.createQuery("""
//                SELECT v
//                FROM Video v
//                WHERE v.user.id > :userId
//                """, Video.class)
//                .setParameter("userId", userId)
//                .getResultList();
//    }

    public List<Video> findVideosByUserId(Long userId) {
        return em.createQuery("""
                SELECT v
                FROM Video v
                WHERE v.user.id = :userId
                """, Video.class)
                .setParameter("userId", userId)
                .getResultList();
    }



}
