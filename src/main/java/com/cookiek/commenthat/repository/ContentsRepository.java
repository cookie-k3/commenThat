package com.cookiek.commenthat.repository;

import com.cookiek.commenthat.dto.ReportDto;
import com.cookiek.commenthat.dto.TopicUrlsDto;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<TopicUrlsDto> getLatestTopicUrlsByUserId(Long userId) {
        // 최신 update_date를 먼저 구함
        LocalDateTime latestUpdateDate = em.createQuery("""
            SELECT MAX(c.updateDate)
            FROM Contents c
            WHERE c.user.id = :userId
            """, LocalDateTime.class)
                .setParameter("userId", userId)
                .getSingleResult();

        // 최신 update_date에 해당하는 contents_id, topic, urls 가져오기
        List<Object[]> results = em.createQuery("""
            SELECT c.id, c.topic, c.urls
            FROM Contents c
            WHERE c.user.id = :userId
              AND c.updateDate >= :latestDate
            """, Object[].class)
                .setParameter("userId", userId)
                .setParameter("latestDate", latestUpdateDate.toLocalDate().atStartOfDay())
                .getResultList();

        List<TopicUrlsDto> dtoList = new ArrayList<>();

        for (Object[] row : results) {
            Long contentsId = (Long) row[0];
            String topic = (String) row[1];
            String urlsRaw = (String) row[2];

            // 공백 제거 후 [와 ]도 제거
            String cleaned = urlsRaw.replace(" ", "").replace("[", "").replace("]", "");

            // 쉼표(,)로 분리 → 리스트로 변환
            List<String> urlsList = Arrays.stream(cleaned.split(","))
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

            dtoList.add(new TopicUrlsDto(contentsId, topic, urlsList));
        }

        return dtoList;
    }

    public ReportDto getReportByContentsId(Long contentsId) {
        Object[] result = em.createQuery("""
            SELECT c.updateDate, c.topic, c.channelAnalysis, c.commentAnalysis, c.topicRec
            FROM Contents c
            WHERE c.id = :contentsId
            """, Object[].class)
                .setParameter("contentsId", contentsId)
                .getSingleResult();

        LocalDateTime updateDate = (LocalDateTime) result[0];
        String topic = (String) result[1];
        String channelAnalysis = (String) result[2];
        String commentAnalysis = (String) result[3];
        String topicRec = (String) result[4];

        return new ReportDto(updateDate, topic, channelAnalysis, commentAnalysis, topicRec);
    }

}
