package com.cookiek.commenthat.service;

import com.cookiek.commenthat.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;

    public Long getRecentVideoIdByUserId(Long userId) {
        return videoRepository.getRecentVideoId(userId);
    }

}
