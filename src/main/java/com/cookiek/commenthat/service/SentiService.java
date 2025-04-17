package com.cookiek.commenthat.service;

import com.cookiek.commenthat.repository.SentiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SentiService {

    private final SentiRepository sentiRepository;

    public List<Long> getSentiCount(Long videoId) {
        return sentiRepository.getSentiCountByVideoId(videoId);
    }



}
