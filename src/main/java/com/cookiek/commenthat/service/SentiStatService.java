package com.cookiek.commenthat.service;

import com.cookiek.commenthat.repository.SentiStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SentiStatService {

    private final SentiStatRepository sentiStatRepository;

//    public List<Long> getSentiCount(Long videoId) {
//        return sentiStatRepository.getSentiCountByVideoId(videoId);
//    }



}
