package com.cookiek.commenthat.service;

import com.cookiek.commenthat.dto.UserRankingDto;
import com.cookiek.commenthat.repository.SentiStatRepository;
import com.cookiek.commenthat.repository.VideoMetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReputationService {

    private final VideoMetaRepository videoMetaRepository;
    private final SentiStatRepository sentiStatRepository;

    // 조회수 기준 상위 유저
    public List<UserRankingDto> getTopUsersByViews() {
        return videoMetaRepository.findTopUsersByTotalViews();
    }

    // 구독자 기준 상위 유저
    public List<UserRankingDto> getTopUsersBySubscribers() {
        return videoMetaRepository.findTopUsersBySubscribers();
    }

    // 긍정 댓글 기준 상위 유저
    public List<UserRankingDto> getTopUsersByPositiveComments() {
        List<Object[]> result = sentiStatRepository.findTopUsersByPositiveComments();

        return result.stream()
                .map(row -> new UserRankingDto(
                        (Long) row[0],   // userId
                        (String) row[1], // loginId
                        (String) row[2], // channelImg
                        (String) row[3], // channelName
                        (Long) row[4]    // total (positive count)
                ))
                .collect(Collectors.toList());
    }
}