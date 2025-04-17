package com.cookiek.commenthat.service;

import com.cookiek.commenthat.dto.CategoryStatCountDto;
import com.cookiek.commenthat.repository.CategoryStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryStatService {

    private final CategoryStatRepository categoryStatRepository;

    public CategoryStatCountDto getCategoryCountByVideoId(Long videoId) {
        return categoryStatRepository.getCategoryStatCountByVideoId(videoId);
    }

    public List<String> getComments(Long video, Long category) {
        return categoryStatRepository.getCategoryCommentsByVideoId(video, category);
    }

    public String getSummary(Long video, Long category) {
        return categoryStatRepository.getCategorySummaryByVideoId(video, category);
    }



}