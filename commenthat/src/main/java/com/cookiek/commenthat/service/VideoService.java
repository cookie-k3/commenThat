package com.cookiek.commenthat.service;

import com.cookiek.commenthat.domain.Video;
import com.cookiek.commenthat.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;

//    public List<Video> findVideosByUserIdGreaterThan(Long userId) {
//        return videoRepository.findVideosByUserIdGreaterThan(userId);
//    }
//
//    public List<Video> findVideosByUserId(Long userId) {
//        return videoRepository.findVideosByUserId(userId);
//    }

}
