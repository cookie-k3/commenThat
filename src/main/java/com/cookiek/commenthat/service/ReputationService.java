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

    private final LocalDateTime start = LocalDate.now().withDayOfMonth(1).atStartOfDay();
    private final LocalDateTime end = LocalDate.now()
            .withDayOfMonth(LocalDate.now().lengthOfMonth())
            .atTime(23, 59, 59, 999_999_000); // 999ms + 999,000ns


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
        log.info("[성실 킹왕짱] 랭킹 계산 시작");

        List<Object[]> rawResult = uploadRepository.findTopUsersByUploadCountNative(start, end);

        for (Object[] row : rawResult) {
            Long userId = ((Number) row[0]).longValue();
            String loginId = (String) row[1];
            String channelName = (String) row[3];
            Number count = (Number) row[4];

            log.info("🔍 Upload Count → userId: {}, loginId: {}, channelName: {}, count: {}", userId, loginId, channelName, count);

            // ✅ userId 6번(침착맨)일 경우, 어떤 영상이 포함되었는지 로그로 출력
            if (userId == 6L) {
                List<com.cookiek.commenthat.domain.Video> videos = uploadRepository.findVideosByUserIdAndDateRange(userId, start, end);
                log.info("📋 침착맨 영상 목록 ({})개:", videos.size());
                for (com.cookiek.commenthat.domain.Video video : videos) {
                    log.info("📽 videoId: {}, uploadDate: {}", video.getId(), video.getDate()); // ✅ 수정된 getter
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
        log.info("[긍정 킹왕짱] 랭킹 계산 시작");
        return sentiStatRepository.findTopUsersByPositiveRatio(start, end).stream()
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