package com.cookiek.commenthat.autoProcessor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class NotSyncService {

    @Value("${youtube.api.key}")
    private String apiKey;

    private static final String SEARCH_URL = "https://www.googleapis.com/youtube/v3/search";
    private static final String CHANNEL_URL = "https://www.googleapis.com/youtube/v3/channels";

    public String getChannelId(String channelName) {
        RestTemplate restTemplate = new RestTemplate();

        URI uri = UriComponentsBuilder.fromHttpUrl(SEARCH_URL)
                .queryParam("part", "snippet")
                .queryParam("type", "channel")
                .queryParam("q", channelName)
                .queryParam("key", apiKey)
                .build()
                .toUri();

        Map<String, Object> response = restTemplate.getForObject(uri, Map.class);
        if (response == null || !response.containsKey("items")) return null;

        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
        if (items.isEmpty()) return null;

        Map<String, Object> firstItem = items.get(0);
        Map<String, Object> id = (Map<String, Object>) firstItem.get("id");

        return (String) id.get("channelId"); // 채널 ID 반환
    }

    public String getChannelImg(String channelName) {
        RestTemplate restTemplate = new RestTemplate();

        URI uri = UriComponentsBuilder.fromHttpUrl(SEARCH_URL)
                .queryParam("part", "snippet")
                .queryParam("type", "channel")
                .queryParam("q", channelName)
                .queryParam("key", apiKey)
                .build()
                .toUri();

        Map<String, Object> response = restTemplate.getForObject(uri, Map.class);
        if (response == null || !response.containsKey("items")) return null;

        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
        if (items.isEmpty()) return null;

        Map<String, Object> firstItem = items.get(0);
        Map<String, Object> snippet = (Map<String, Object>) firstItem.get("snippet");
        if (snippet == null || !snippet.containsKey("thumbnails")) return null;

        Map<String, Object> thumbnails = (Map<String, Object>) snippet.get("thumbnails");

        String thumbnailUrl = null;
        String[] qualityOrder = { "maxres", "high", "medium", "default" };

        for (String quality : qualityOrder) {
            if (thumbnails.get(quality) != null) {
                thumbnailUrl = (String) ((Map<String, Object>) thumbnails.get(quality)).get("url");
                break;
            }
        }

        return thumbnailUrl; // 썸네일 URL 반환
    }



    // 채널 설정에서 구독자 수를 숨기거나 정책상 노출이 제한되어서 1000 단위로 반올림된 숫자를 보여줌
    public Long getSubscriberCount(String channelId) {
        RestTemplate restTemplate = new RestTemplate();

        URI uri = UriComponentsBuilder.fromHttpUrl(CHANNEL_URL)
                .queryParam("part", "statistics")
                .queryParam("id", channelId)
                .queryParam("key", apiKey)
                .build()
                .toUri();

        Map<String, Object> response = restTemplate.getForObject(uri, Map.class);
        if (response == null || !response.containsKey("items")) return null;

        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
        if (items.isEmpty()) return null;

        Map<String, Object> statistics = (Map<String, Object>) items.get(0).get("statistics");
        return Long.parseLong((String) statistics.getOrDefault("subscriberCount", "0"));
    }

}