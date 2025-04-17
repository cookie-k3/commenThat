//package com.cookiek.commenthat.repository;
//
//import jakarta.persistence.EntityManager;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Repository;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Repository
//@RequiredArgsConstructor
//public class SentiRepository {
//
//    private final EntityManager em;
//
//    public List<Long> getSentiCountByVideoId(Long videoId) {
//        List<Long> result = new ArrayList<>(List.of(0L, 0L)); // 0: 부정, 1: 긍정
//
//        List<Object[]> counts = em.createQuery("""
//        SELECT s.isPositive, s.count
//        FROM SentiStat s
//        WHERE s.video.id = :videoId
//        """, Object[].class)
//                .setParameter("videoId", videoId)
//                .getResultList();
//
//        for (Object[] row : counts) {
//            Integer isPositive = (Integer) row[0];
//            Long count = (Long) row[1];
//            if (isPositive != null && (isPositive == 0 || isPositive == 1)) {
//                result.set(isPositive, count);
//            }
//        }
//
//        return result;
//    }
//
//
//}
