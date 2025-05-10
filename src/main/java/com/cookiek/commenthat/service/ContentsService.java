package com.cookiek.commenthat.service;

import com.cookiek.commenthat.dto.ReportDto;
import com.cookiek.commenthat.dto.TopicUrlsDto;
import com.cookiek.commenthat.repository.ContentsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContentsService {

    private final ContentsRepository contentsRepository;

    public List<String> getTopics(Long userId) {
        return contentsRepository.getTopicsByUserId(userId);
    }

    public List<TopicUrlsDto> getTopicUrls(Long userId) {
        return contentsRepository.getLatestTopicUrlsByUserId(userId);
    }

//    public ReportDto getReport(Long contentsId) {
//        return contentsRepository.getReportByContentsId(contentsId);
//    }

    public ReportDto getContentsSummary(Long userId) {
        return contentsRepository.getSummaryContentsByUserId(userId);
    }


}
