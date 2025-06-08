package com.cookiek.commenthat.service;

import com.cookiek.commenthat.dto.VideoDto;
import com.cookiek.commenthat.repository.VideoMetaRepository;
import com.cookiek.commenthat.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;
    private final VideoMetaRepository videoMetaRepository;

    public Long getRecentVideoIdByUserId(Long userId) {
        return videoRepository.getRecentVideoId(userId);
    }

    public Long getMostRecentAnalyzedVideoId(Long userId) {
        return videoRepository.getMostRecentAnalyzedVideoId(userId);
    }

    public List<VideoDto> getVideoList(Long userId) {
        return videoRepository.getVideoList(userId);
    }

}