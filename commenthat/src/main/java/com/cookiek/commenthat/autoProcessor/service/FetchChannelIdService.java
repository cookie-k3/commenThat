package com.cookiek.commenthat.autoProcessor.service;

import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class FetchChannelIdService {

    @Value("${youtube.api.key}")
    private String apiKey;

    private static final String SEARCH_URL = "https://www.googleapis.com/youtube/v3/search";

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

}


//    private Mono<String> getChannelId(String channelName) {
//        return webClient.get()
//                .uri(uriBuilder -> uriBuilder
//                        .path("/search")
//                        .queryParam("part", "snippet")
//                        .queryParam("q", channelName)
//                        .queryParam("type", "channel")
//                        .queryParam("key", apiKey)
//                        .build())
//                .retrieve()
//                .bodyToMono(Map.class)
//                .flatMap(response -> {
//                    if (response.containsKey("items")) {
//                        Map<String, Object> item = ((List<Map<String, Object>>) response.get("items")).get(0);
//                        Map<String, Object> idMap = (Map<String, Object>) item.get("id");
//                        return Mono.justOrEmpty((String) idMap.get("channelId"));
//                    }
//                    return Mono.empty();
//                });
//    }

