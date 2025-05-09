package com.cookiek.commenthat.service;

import com.cookiek.commenthat.dto.UserRankingDto;
import com.cookiek.commenthat.repository.SentiStatRepository;
import com.cookiek.commenthat.repository.UploadRepository;
import com.cookiek.commenthat.repository.ViewGrowthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReputationService {

    private final ViewGrowthRepository viewGrowthRepository;
    private final UploadRepository uploadRepository;
    private final SentiStatRepository sentiStatRepository;

    private final LocalDateTime start = LocalDate.now().withDayOfMonth(1).atStartOfDay();
    private final LocalDateTime end = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).atTime(23, 59, 59);

    public List<UserRankingDto> getTopUsersByViews() {
        log.info("[조회수 킹왕짱] 랭킹 계산 시작");
        return viewGrowthRepository.findTopUsersByViewGrowthRateNative(start, end).stream()
                .map(row -> new UserRankingDto(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        (String) row[2],
                        (String) row[3],
                        ((Number) row[4]).longValue()
                )).collect(Collectors.toList());
    }

    public List<UserRankingDto> getTopUsersByUploadCount() {
        log.info("[성실 킹왕짱] 랭킹 계산 시작");
        return uploadRepository.findTopUsersByUploadCountNative(start, end).stream()
                .map(row -> new UserRankingDto(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        (String) row[2],
                        (String) row[3],
                        ((Number) row[4]).longValue()
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
                        Math.round((Double) row[4] * 100) // 백분율
                )).collect(Collectors.toList());
    }
}