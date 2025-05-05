package com.cookiek.commenthat.repository;

import com.cookiek.commenthat.autoProcessor.dto.PositiveCommentDto;
import com.cookiek.commenthat.domain.SentiStat;
import com.cookiek.commenthat.domain.Video;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class SentiRepository {

    @PersistenceContext
    private EntityManager em;

    public List<String> findCommentsTextByVideoId(Long videoId, Long isPositive) {
        return em.createQuery("""
            SELECT c.comment
            FROM VideoComment c
            WHERE c.video.id = :videoId AND c.isPositive = :isPositive
            """, String.class)
                .setParameter("videoId", videoId)
                .setParameter("isPositive", isPositive)
                .getResultList();
    }

//    public List<PositiveCommentDto> findPositiveCommentsByVideoId(Long videoId) {
//        List<Object[]> results = em.createQuery("""
//            SELECT vc.comment, vc.likeCount
//            FROM VideoComment vc
//            WHERE vc.video.id = :videoId AND vc.isPositive = 1
//            """, Object[].class)
//                .setParameter("videoId", videoId)
//                .getResultList();
//        return results.stream()
//                .map(row -> new PositiveCommentDto((String) row[0], (Long) row[1]))
//                .collect(Collectors.toList());
//    }

    @Transactional
    public void savePositiveWords(Long videoId, List<PositiveCommentDto> words, Long isPositive) {
        // Video 엔티티 조회
        Video video = em.find(Video.class, videoId);
        if (video == null) {
            throw new IllegalArgumentException("Video not found with id: " + videoId);
        }

        // Positive 단어들과 갯수를 [a:3, b:5] 형태로 변환
        String keywordsJson = convertDtoListToJson(words);

        SentiStat sentiStat = new SentiStat();
        sentiStat.setVideo(video);
        sentiStat.setIsPositive(isPositive.intValue());
        sentiStat.setCount((long) words.size());
        sentiStat.setKeywords(keywordsJson);

        em.persist(sentiStat);
    }

    private String convertDtoListToJson(List<PositiveCommentDto> list) {
        return "[" + list.stream()
                .map(dto -> "{\"text\":\"" + dto.getText() + "\",\"value\":" + dto.getValue() + "}")
                .collect(Collectors.joining(",")) + "]";
    }
}
