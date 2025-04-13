package com.cookiek.commenthat.service;

import com.cookiek.commenthat.dto.CategoryStatCountDto;
import com.cookiek.commenthat.repository.CategoryStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryStatService {

    private final CategoryStatRepository categoryStatRepository;

    public CategoryStatCountDto getCategoryCountByVideoId(Long videoId) {
        return categoryStatRepository.getCategoryStatCountByVideoId(videoId);
    }



}
