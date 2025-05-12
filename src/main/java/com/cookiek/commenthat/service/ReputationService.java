package com.cookiek.commenthat.service;

import com.cookiek.commenthat.dto.UserRankingDto;
import com.cookiek.commenthat.repository.SentiStatRepository;
import com.cookiek.commenthat.repository.UploadRepository;
import com.cookiek.commenthat.repository.ViewGrowthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReputationService {

    private final ViewGrowthRepository viewGrowthRepository;
    private final UploadRepository uploadRepository;
    private final SentiStatRepository sentiStatRepository;

    // 지난달 기준으로 시작일, 종료일 계산
    private final LocalDateTime prevMonthStart = LocalDate.now().minusMonths(1).withDayOfMonth(1).atStartOfDay();

    private final LocalDateTime prevMonthEnd = LocalDate.now().minusMonths(1)
            .withDayOfMonth(LocalDate.now().minusMonths(1).lengthOfMonth())
            .atTime(23, 59, 59, 999_999_000); // 999ms + 999,000ns

//    private final LocalDateTime start = LocalDate.now().withDayOfMonth(1).atStartOfDay();
//    private final LocalDateTime end = LocalDate.now()
//            .withDayOfMonth(LocalDate.now().lengthOfMonth())
//            .atTime(23, 59, 59, 999_999_000); // 999ms + 999,000ns


    public List<UserRankingDto> getTopUsersByViews() {
        log.info("[조회수 킹왕짱] 최근 3일 기준 증가율 랭킹 계산 시작");

        return viewGrowthRepository.findTopUsersByViewGrowthRateLast3Days().stream()
                .map(row -> new UserRankingDto(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        (String) row[2],
                        (String) row[3],
                        ((BigDecimal) row[4])
                                .multiply(BigDecimal.valueOf(100))
                                .setScale(2, RoundingMode.HALF_UP)
                )).collect(Collectors.toList());
    }

    public List<UserRankingDto> getTopUsersByUploadCount() {
        log.info("[성실 킹왕짱] 지난달 기준 랭킹 계산 시작");

        List<Object[]> rawResult = uploadRepository.findTopUsersByUploadCountNative(prevMonthStart, prevMonthEnd);

        for (Object[] row : rawResult) {
            Long userId = ((Number) row[0]).longValue();
            String loginId = (String) row[1];
            String channelName = (String) row[3];
            Number count = (Number) row[4];

            log.info("🔍 Upload Count → userId: {}, loginId: {}, channelName: {}, count: {}", userId, loginId, channelName, count);

            if (userId == 6L) {
                List<com.cookiek.commenthat.domain.Video> videos = uploadRepository.findVideosByUserIdAndDateRange(userId, prevMonthStart, prevMonthEnd);
                log.info("📋 침착맨 영상 목록 ({})개:", videos.size());
                for (com.cookiek.commenthat.domain.Video video : videos) {
                    log.info("📽 videoId: {}, uploadDate: {}", video.getId(), video.getDate());
                }
            }
        }

        return rawResult.stream()
                .map(row -> new UserRankingDto(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        (String) row[2],
                        (String) row[3],
                        BigDecimal.valueOf(((Number) row[4]).longValue())
                )).collect(Collectors.toList());
    }

    public List<UserRankingDto> getTopUsersByPositiveComments() {
        log.info("[긍정 킹왕짱] 지난달 기준 랭킹 계산 시작");

        // 쿼리 결과 받아오기
        List<Object[]> result = sentiStatRepository.findTopUsersByPositiveRatio(prevMonthStart, prevMonthEnd);

        // 👉 로그로 쿼리 결과 출력
        log.info("총 긍정 랭킹 유저 수: {}", result.size());
        for (Object[] row : result) {
            Long userId = ((Number) row[0]).longValue();
            String loginId = (String) row[1];
            String channelName = (String) row[3];
            BigDecimal ratio = (BigDecimal) row[4];

            log.info("👍 긍정 비율 유저 → userId: {}, loginId: {}, 채널명: {}, 비율: {}",
                    userId, loginId, channelName, ratio);
        }

        // DTO 변환
        return result.stream()
                .map(row -> new UserRankingDto(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        (String) row[2],
                        (String) row[3],
                        ((BigDecimal) row[4])
                                .multiply(BigDecimal.valueOf(100))
                                .setScale(2, RoundingMode.HALF_UP)
                )).collect(Collectors.toList());
    }


}