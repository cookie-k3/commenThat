package com.cookiek.commenthat.repository;

import com.cookiek.commenthat.domain.CategoryStat;
import com.cookiek.commenthat.dto.CategoryStatCountDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CategoryStatRepository {

    private EntityManager em;

    public CategoryStatCountDto getCategoryStatCountByVideoId(Long videoId) {
        List<Long> counts = em.createQuery("""
            SELECT cs.count
            FROM CategoryStat cs
            WHERE cs.video.id = :videoId
            ORDER BY cs.category.id ASC
            """, Long.class)
                .setParameter("videoId", videoId)
                .getResultList();


        // 리스트 길이 검증 (필수: 14개여야 함)
        if (counts.size() != 14) {
            throw new IllegalStateException("category_stat 테이블의 데이터 수가 15개가 아닙니다. 현재 개수: " + counts.size());
        }

        return new CategoryStatCountDto(
                counts.get(0).toString(),  // joy
                counts.get(1).toString(),  // sadness
                counts.get(2).toString(),  // anger
                counts.get(3).toString(),  // fear
                counts.get(4).toString(),  // happiness
                counts.get(5).toString(),  // cheering
                counts.get(6).toString(),  // concern
                counts.get(7).toString(),  // sympathy
                counts.get(8).toString(),  // congratulations
                counts.get(9).toString(),  // question
                counts.get(10).toString(), // suggestion
                counts.get(11).toString(), // praise
                counts.get(12).toString(), // hate
                counts.get(13).toString()  // other
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
