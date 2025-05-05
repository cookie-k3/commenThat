package com.cookiek.commenthat.repository;

import com.cookiek.commenthat.domain.CategoryStat;
import com.cookiek.commenthat.dto.CategoryStatCountDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CategoryStatRepository {

    private final EntityManager em;

    public CategoryStatCountDto getCategoryStatCountByVideoId(Long videoId) {
        // 카테고리 수 10개로 변경 (기존: 14개)
        List<Long> counts = new ArrayList<>(Collections.nCopies(10, 0L)); // 길이 10, 0으로 초기화

        List<Object[]> results = em.createQuery("""
            SELECT cs.category.id, cs.count
            FROM CategoryStat cs
            WHERE cs.video.id = :videoId
            """, Object[].class)
                .setParameter("videoId", videoId)
                .getResultList();

        for (Object[] row : results) {
            Long categoryId = (Long) row[0];
            Long count = (Long) row[1];

            // 유효 범위 수정: 1~10
            if (categoryId != null && categoryId >= 1 && categoryId <= 10) {
                int index = categoryId.intValue() - 1; // categoryId 1 → index 0
                counts.set(index, count);
            }
        }

        // 10개 카테고리에 맞춰 매핑
        return new CategoryStatCountDto(
                counts.get(0).toString(),  // joy
                counts.get(1).toString(),  // supportive
                counts.get(2).toString(),  // suggestion
                counts.get(3).toString(),  // hate
                counts.get(4).toString(),  // question
                counts.get(5).toString(),  // praise
                counts.get(6).toString(),  // sympathy
                counts.get(7).toString(),  // congratulations
                counts.get(8).toString(),  // concern
                counts.get(9).toString()   // other
        );
    }

    public List<String> getCategoryCommentsByVideoId(Long videoId, Long categoryId) {
        String jpql = "SELECT vc.comment FROM VideoComment vc WHERE vc.video.id = :videoId AND vc.category.id = :categoryId";
        return em.createQuery(jpql, String.class)
                .setParameter("videoId", videoId)
                .setParameter("categoryId", categoryId)
                .getResultList();
    }

    public String getCategorySummaryByVideoId(Long videoId, Long categoryId) {
        return em.createQuery("""
            SELECT cs.summary
            FROM CategoryStat cs
            WHERE cs.video.id = :videoId AND cs.category.id = :categoryId
            """, String.class)
                .setParameter("videoId", videoId)
                .setParameter("categoryId", categoryId)
                .getSingleResult();
    }
}