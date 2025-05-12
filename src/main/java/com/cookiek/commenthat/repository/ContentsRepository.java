package com.cookiek.commenthat.repository;

import com.cookiek.commenthat.domain.Contents;
import com.cookiek.commenthat.domain.Reference;
import com.cookiek.commenthat.dto.ReferenceDto;
import com.cookiek.commenthat.dto.ReportDto;
import com.cookiek.commenthat.dto.TopicUrlsDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Repository
@RequiredArgsConstructor
public class ContentsRepository {

    private final EntityManager em;

    public List<String> getLatestTopicsByUserId(Long userId) {
        String jsonTopic = em.createQuery("""
        SELECT c.topic
        FROM Contents c
        WHERE c.user.id = :userId
        ORDER BY c.updateDate DESC
        """, String.class)
                .setParameter("userId", userId)
                .setMaxResults(1)
                .getSingleResult();

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(jsonTopic, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

//    public List<String> getTopicsByUserId(Long userId) {
//        return em.createQuery("""
//        SELECT c.topic
//        FROM Contents c
//        WHERE c.user.id = :userId
//        """, String.class)
//                .setParameter("userId", userId)
//                .getResultList();
//    }

    public List<TopicUrlsDto> getLatestTopicUrlsByUserId(Long userId) {
        // 1) 최신 update_date 가져오기
        LocalDateTime latestUpdateDate = em.createQuery("""
        SELECT MAX(c.updateDate)
        FROM Contents c
        WHERE c.user.id = :userId
    """, LocalDateTime.class)
                .setParameter("userId", userId)
                .getSingleResult();

        if (latestUpdateDate == null) {
            return Collections.emptyList();
        }

        // 2) 날짜 범위 설정
        LocalDate latestDate = latestUpdateDate.toLocalDate();
        LocalDateTime startOfDay = latestDate.atStartOfDay();
        LocalDateTime nextDayStart = startOfDay.plusDays(1);

        // 3) 최신 Contents 가져오기
        List<Contents> contentsList = em.createQuery("""
        SELECT DISTINCT c
        FROM Contents c
        LEFT JOIN FETCH c.references r
        WHERE c.user.id = :userId
          AND c.updateDate >= :startOfDay
          AND c.updateDate < :nextDayStart
    """, Contents.class)
                .setParameter("userId", userId)
                .setParameter("startOfDay", startOfDay)
                .setParameter("nextDayStart", nextDayStart)
                .getResultList();

        if (contentsList.isEmpty()) {
            return Collections.emptyList();
        }

        Contents latestContents = contentsList.get(0);

        // 4) Topic 리스트 파싱
        List<String> topics = parseJsonToList(latestContents.getTopic());

        // 5) Reference 데이터 인덱스별 그룹화 (Long → int 변환)
        Map<Integer, List<ReferenceDto>> groupedReferences = latestContents.getReferences().stream()
                .collect(Collectors.groupingBy(
                        r -> r.getIdx().intValue(), // Long → int 변환
                        Collectors.mapping(r -> new ReferenceDto(
                                r.getImg(),
                                r.getTitle(),
                                r.getUrl(),
                                r.getViews(),
                                r.getIdx().intValue() // Long → int 변환
                        ), Collectors.toList())
                ));

        // 6) TopicUrlsDto로 변환 (idx와 topic 매칭)
        return IntStream.range(0, topics.size())
                .mapToObj(i -> new TopicUrlsDto(
                        latestContents.getId(),
                        topics.get(i),
                        groupedReferences.getOrDefault(i, Collections.emptyList())
                ))
                .collect(Collectors.toList());
    }

    private final ObjectMapper mapper = new ObjectMapper();

    // ✅ JSON 파싱을 위한 헬퍼 메서드 (반드시 final 변수 사용)
    private List<String> parseJsonToList(String json) {
        try {
            return mapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }


    public ReportDto getSummaryContentsByUserId(Long userId) {
        Object[] result = em.createQuery("""
         SELECT c.updateDate, c.topic, c.topicAnalysis, c.topicRec, 
                c.topViewVideo, c.topPositiveVideo, c.topNegativeVideo, 
                c.positiveKeywords, c.topCategories
         FROM Contents c
         WHERE c.user.id = :userId
         ORDER BY c.updateDate DESC
    """, Object[].class)
                .setParameter("userId", userId)
                .setMaxResults(1)  // 최신 1개만 가져옴
                .getSingleResult();

        LocalDateTime updateDate = (LocalDateTime) result[0];
        String topic = (String) result[1];
        String topicAnalysis = (String) result[2];
        String topicRec = (String) result[3];
        String topViewVideo = (String) result[4];
        String topPositiveVideo = (String) result[5];
        String topNegativeVideo = (String) result[6];
        String topPositiveKeywords = (String) result[7];
        String topCategories = (String) result[8];

        return new ReportDto(updateDate, topic, topicAnalysis, topicRec,
                topViewVideo, topPositiveVideo, topNegativeVideo,
                topPositiveKeywords, topCategories);
    }

}