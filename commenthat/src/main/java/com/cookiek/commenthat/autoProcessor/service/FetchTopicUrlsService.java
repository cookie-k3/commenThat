package com.cookiek.commenthat.autoProcessor.service;

import com.cookiek.commenthat.autoProcessor.dto.ReferenceDto;
import com.cookiek.commenthat.autoProcessor.dto.TopicUrlsDto;
import com.cookiek.commenthat.domain.Contents;
import com.cookiek.commenthat.domain.Reference;
import com.cookiek.commenthat.repository.ContentsInterface;
import com.cookiek.commenthat.repository.ReferenceInterface;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FetchTopicUrlsService {

    private final ContentsInterface contentsInterface;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ReferenceInterface referenceInterface;

    @Value("${youtube.api.key}")
    private String apiKey1;

    @Value("${youtube.api.key2}")
    private String apiKey2;

    private List<String> apiKeys;

    @PostConstruct
    public void initApiKeys() {
        apiKeys = new ArrayList<>();
        if (apiKey1 != null) apiKeys.add(apiKey1);
        if (apiKey2 != null) apiKeys.add(apiKey2);
    }

    @Async
    @Transactional
    public void updateUrlsAsync(Long contentsId) {
        Contents contents = contentsInterface.findById(contentsId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 contentsId: " + contentsId));

        String topic = contents.getTopic();
        String channelId = contents.getUser().getChannelId();

        List<ReferenceDto> referenceDtos = fetchYoutubeVideoDetails(topic, 4, channelId);

        try {
            // Dto → Entity 변환
            List<Reference> references = referenceDtos.stream()
                    .map(dto -> {
                        Reference ref = new Reference();
                        ref.setContents(contents);
                        ref.setTitle(dto.getTitle());
                        ref.setUrl(dto.getUrl());
                        ref.setViews(dto.getViews());
                        ref.setImg(dto.getImg());
                        return ref;
                    })
                    .toList();

            referenceInterface.saveAll(references);

            log.info("contentsId {}의 URL 업데이트 완료", contentsId);
        } catch (Exception e) {
            log.error("contentsId {}의 URL JSON 변환 실패: {}", contentsId, e.getMessage());
        }
    }

    public List<ReferenceDto> fetchYoutubeVideoDetails(String topic, int maxResults, String excludedChannelId) {

        List<ReferenceDto> referenceDtos = new ArrayList<>();
        Set<String> seenVideoIds = new HashSet<>(); // 이미 본 videoId 기록용

        for (String apiKey : apiKeys) {
            String searchApiUrl = "https://www.googleapis.com/youtube/v3/search"
                    + "?part=snippet&type=video&maxResults=" + (maxResults * 2) // 여유 있게 요청
                    + "&q=" + topic + "&key=" + apiKey;

            try {
                String response = restTemplate.getForObject(searchApiUrl, String.class);
                JsonNode root = objectMapper.readTree(response);

                if (root.has("error")) {
                    String reason = root.path("error").path("errors").get(0).path("reason").asText();
                    if ("quotaExceeded".equals(reason)) {
                        log.warn("API 키 {}의 할당량이 초과되었습니다. 다음 키로 시도합니다.", apiKey);
                        continue;
                    } else {
                        log.error("API 키 {}에서 오류 발생: {}", apiKey, reason);
                        break;
                    }
                }

                JsonNode items = root.get("items");
                if (items != null && items.isArray()) {
                    List<String> videoIds = new ArrayList<>();

                    for (JsonNode item : items) {
                        String videoId = item.path("id").path("videoId").asText();
                        if (!videoId.isEmpty()) {
                            videoIds.add(videoId);
                        }
                    }

                    String videosApiUrl = "https://www.googleapis.com/youtube/v3/videos"
                            + "?part=snippet,statistics&id=" + String.join(",", videoIds)
                            + "&key=" + apiKey;

                    String videosResponse = restTemplate.getForObject(videosApiUrl, String.class);
                    JsonNode videosRoot = objectMapper.readTree(videosResponse);

                    JsonNode videoItems = videosRoot.get("items");
                    if (videoItems != null && videoItems.isArray()) {
                        for (JsonNode videoItem : videoItems) {
                            if (referenceDtos.size() >= maxResults) {
                                break;
                            }

                            String videoId = videoItem.path("id").asText();
                            String channelId = videoItem.path("snippet").path("channelId").asText();

                            // 제외할 채널이면 건너뛰기
                            if (excludedChannelId != null && excludedChannelId.equals(channelId)) {
                                continue;
                            }

                            // 이미 수집된 videoId면 건너뛰기
                            if (seenVideoIds.contains(videoId)) {
                                continue;
                            }
                            seenVideoIds.add(videoId); // 새 videoId 기록

                            String title = videoItem.path("snippet").path("title").asText();
                            Long viewCount = videoItem.path("statistics").path("viewCount").asLong();

                            JsonNode thumbnails = videoItem.path("snippet").path("thumbnails");
                            String thumbnailUrl = "";
                            if (thumbnails.has("maxres")) {
                                thumbnailUrl = thumbnails.path("maxres").path("url").asText();
                            } else if (thumbnails.has("standard")) {
                                thumbnailUrl = thumbnails.path("standard").path("url").asText();
                            } else if (thumbnails.has("high")) {
                                thumbnailUrl = thumbnails.path("high").path("url").asText();
                            } else if (thumbnails.has("medium")) {
                                thumbnailUrl = thumbnails.path("medium").path("url").asText();
                            } else if (thumbnails.has("default")) {
                                thumbnailUrl = thumbnails.path("default").path("url").asText();
                            }

                            ReferenceDto referenceDto = new ReferenceDto();
                            referenceDto.setUrl("https://www.youtube.com/watch?v=" + videoId);
                            referenceDto.setTitle(title);
                            referenceDto.setViews(viewCount);
                            referenceDto.setImg(thumbnailUrl);

                            referenceDtos.add(referenceDto);
                        }
                    }
                }

            } catch (Exception e) {
                log.error("API 키 {}에서 예외 발생: {}", apiKey, e.getMessage());
            }

            if (referenceDtos.size() >= maxResults) {
                break; // 성공했으면 종료
            }
        }

        if (referenceDtos.isEmpty()) {
            log.error("모든 API 키에서 데이터를 가져오지 못했습니다.");
        }

        return referenceDtos;
    }




}
