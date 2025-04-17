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

//    public Long getRecentVideoId(Long userId) {
//        return em.createQuery("""
//            SELECT v.id
//            FROM Video v
//            WHERE v.user.id = :userId
//            ORDER BY v.date DESC
//            """, Long.class)
//                .setParameter("userId", userId)
//                .setMaxResults(1)  // 가장 최근 1개만
//                .getSingleResult();
//    } 영상이 없을 경우 javax.persistence.NoResultException 발생 -> catch에서 잡히기 전에 터짐
//    -> Axios 요청이 500 에러로 끝나게 됨
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
            .orElse(null); // 결과가 없을 땐 null 반환
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
