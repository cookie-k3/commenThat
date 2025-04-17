package com.cookiek.commenthat.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ContentsRepository {

    private final EntityManager em;

    public List<String> getTopicsByUserId(Long userId) {
        return em.createQuery("""
        SELECT c.topic
        FROM Contents c
        WHERE c.user.id = :userId
        """, String.class)
                .setParameter("userId", userId)
                .getResultList();
    }

}
