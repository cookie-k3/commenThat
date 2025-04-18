package com.cookiek.commenthat.repository;

import com.cookiek.commenthat.dto.PositiveCommentDto;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class SentiRepository {

    private final EntityManager em;

    public List<Long> getSentiCountByVideoId(Long videoId) {
        List<Long> result = new ArrayList<>(List.of(0L, 0L)); // 0: 부정, 1: 긍정

        List<Object[]> counts = em.createQuery("""
            SELECT vc.isPositive, COUNT(vc)
            FROM VideoComment vc
            WHERE vc.video.id = :videoId
            GROUP BY vc.isPositive
            """, Object[].class)
                .setParameter("videoId", videoId)
                .getResultList();

        for (Object[] row : counts) {
            Integer isPositive = (Integer) row[0]; // 0 or 1
            Long count = (Long) row[1];
            result.set(isPositive, count);
        }

        return result;
    }

    public List<PositiveCommentDto> findPositiveCommentsByVideoId(Long videoId) {
        List<Object[]> results = em.createQuery("""
            SELECT vc.comment, vc.likeCount
            FROM VideoComment vc
            WHERE vc.video.id = :videoId AND vc.isPositive = 1
            """, Object[].class)
                .setParameter("videoId", videoId)
                .getResultList();

        return results.stream()
                .map(row -> new PositiveCommentDto((String) row[0], (Long) row[1]))
                .collect(Collectors.toList());
    }





}
