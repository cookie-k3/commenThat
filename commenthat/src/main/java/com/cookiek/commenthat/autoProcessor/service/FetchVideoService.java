package com.cookiek.commenthat.autoProcessor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FetchVideoService {

    @Value("${youtube.api.key}")
    private String apiKey;

}
