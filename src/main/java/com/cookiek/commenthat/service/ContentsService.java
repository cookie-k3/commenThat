package com.cookiek.commenthat.service;

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

}
