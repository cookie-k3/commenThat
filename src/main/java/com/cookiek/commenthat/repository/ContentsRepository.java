package com.cookiek.commenthat.repository;

import com.cookiek.commenthat.domain.Contents;
import com.cookiek.commenthat.dto.ReferenceDto;
import com.cookiek.commenthat.dto.ReportDto;
import com.cookiek.commenthat.dto.TopicUrlsDto;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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

    public List<TopicUrlsDto> getLatestTopicUrlsByUserId(Long userId) {
        // 최신 update_date를 먼저 구함 -> 가장 최신 토픽들을 제공하기 위해
        /* 1) 사용자의 가장 최근 update_date 구하기 */
        LocalDateTime latestUpdateDate = em.createQuery("""
        SELECT MAX(c.updateDate)
        FROM Contents c
        WHERE c.user.id = :userId
        """, LocalDateTime.class)
                .setParameter("userId", userId)
                .getSingleResult();

        if (latestUpdateDate == null) {          // 해당 사용자의 데이터가 없는 경우
            return Collections.emptyList();
        }

        // 2) 날짜 범위 계산
        LocalDate latestDate     = latestUpdateDate.toLocalDate();   // yyyy-MM-dd
        LocalDateTime startOfDay   = latestDate.atStartOfDay();        // 00:00:00.000
        LocalDateTime nextDayStart = startOfDay.plusDays(1);           // 다음 날 00:00

        // 3) 범위 조건으로 Contents + Reference 로드
        List<Contents> contentsList = em.createQuery("""
        SELECT DISTINCT c
        FROM Contents c
        LEFT JOIN FETCH c.references r
        WHERE c.user.id      = :userId
          AND c.updateDate  >= :startOfDay
          AND c.updateDate  <  :nextDayStart
        """, Contents.class)
                .setParameter("userId",       userId)
                .setParameter("startOfDay",   startOfDay)
                .setParameter("nextDayStart", nextDayStart)
                .getResultList();

        /* 4) DTO 변환 */
        return contentsList.stream()
                .map(c -> new TopicUrlsDto(
                        c.getId(),
                        c.getTopic(),
                        c.getReferences()
                                .stream()
                                .map(r -> new ReferenceDto(
                                        r.getImg(),
                                        r.getTitle(),
                                        r.getUrl(),
                                        r.getViews()))
                                .toList()
                ))
                .toList();
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