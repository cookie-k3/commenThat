package com.cookiek.commenthat.service;

import com.cookiek.commenthat.domain.SentiStat;
import com.cookiek.commenthat.dto.PositiveCommentDto;
import com.cookiek.commenthat.repository.SentiStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SentiService {

    private final SentiStatRepository sentiStatRepository;

    // 긍/부정 댓글 수 조회 (DB에 저장된 결과가 있으면 가져오고, 없으면 null 반환)
    public List<Long> getSentiCount(Long videoId) {
        List<SentiStat> stats = sentiStatRepository.findByVideoId(videoId);
        if (stats.isEmpty()) return null; //
        // 분석 결과 없음 처리

        Long negative = stats.stream()
                .filter(s -> s.getIsPositive() == 0)
                .map(SentiStat::getCount)
                .findFirst()
                .orElse(0L);

        Long positive = stats.stream()
                .filter(s -> s.getIsPositive() == 1)
                .map(SentiStat::getCount)
                .findFirst()
                .orElse(0L);

        return List.of(negative, positive);
    }

    // 긍정 댓글 키워드 추출 (DB에 저장된 키워드가 있으면 가져오고, 없으면 null 반환)
    public List<PositiveCommentDto> getPositiveWord(Long videoId) {
        return sentiStatRepository.findByVideoId(videoId).stream()
                .filter(s -> s.getIsPositive() == 1 && s.getKeywords() != null)
                .findFirst()
                .map(stat -> parseJsonToDtoList(stat.getKeywords()))
                .orElse(null); // 분석 결과 없음 처리
    }

    // JSON 문자열을 PositiveCommentDto 리스트로 변환
    private List<PositiveCommentDto> parseJsonToDtoList(String json) {
        if (json == null || json.isBlank()) return List.of();

        // JSON 파싱 로직 개선 (예외 방지)
        json = json.trim();
        if (!json.startsWith("[") || !json.endsWith("]")) return List.of();

        json = json.substring(1, json.length() - 1); // 양쪽 대괄호 제거
        String[] items = json.split("\\},\\{");

        List<PositiveCommentDto> result = new ArrayList<>();
        for (String item : items) {
            item = item.replace("{", "").replace("}", "");
            String[] fields = item.split(",");

            String text = null;
            Long value = null;

            for (String field : fields) {
                String[] keyValue = field.split(":");
                if (keyValue.length < 2) continue; // 잘못된 필드 스킵
                String key = keyValue[0].trim().replace("\"", "");
                String val = keyValue[1].trim().replace("\"", "");

                if ("text".equals(key)) {
                    text = val;
                } else if ("value".equals(key)) {
                    try {
                        value = Long.parseLong(val);
                    } catch (NumberFormatException e) {
                        value = 0L; // 숫자 변환 실패 시 기본값
                    }
                }
            }

            if (text != null && value != null) {
                result.add(new PositiveCommentDto(text, value));
            }
        }
        return result;
    }
}