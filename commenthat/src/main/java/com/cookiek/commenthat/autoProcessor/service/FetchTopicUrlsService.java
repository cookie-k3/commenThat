package com.cookiek.commenthat.autoProcessor.service;

import com.cookiek.commenthat.domain.Contents;
import com.cookiek.commenthat.repository.ContentsInterface;
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

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FetchTopicUrlsService {

    private final ContentsInterface contentsInterface;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

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
        List<String> urls = fetchYoutubeUrls(topic, 20);

        try {
            String urlsAsJson = objectMapper.writeValueAsString(urls);  // ["url1", "url2", ...]
            contents.setUrls(urlsAsJson);
            contentsInterface.save(contents);

            log.info("contentsId {}의 URL 업데이트 완료", contentsId);
        } catch (Exception e) {
            log.error("contentsId {}의 URL JSON 변환 실패: {}", contentsId, e.getMessage());
        }
    }

    public List<String> fetchYoutubeUrls(String topic, int maxResults) {
        List<String> urls = new ArrayList<>();

        for (String apiKey : apiKeys) {
            String apiUrl = "https://www.googleapis.com/youtube/v3/search"
                    + "?part=snippet&type=video&maxResults=" + maxResults
                    + "&q=" + topic + "&key=" + apiKey;

            try {
                String response = restTemplate.getForObject(apiUrl, String.class);
                JsonNode root = objectMapper.readTree(response);

                // quotaExceeded 체크
                if (root.has("error")) {
                    String reason = root.path("error").path("errors").get(0).path("reason").asText();
                    if ("quotaExceeded".equals(reason)) {
                        log.warn("API 키 {}의 할당량이 초과되었습니다. 다음 키로 시도합니다.", apiKey);
                        continue;  // 다음 키로 넘어감
                    } else {
                        log.error("API 키 {}에서 오류 발생: {}", apiKey, reason);
                        break;  // quotaExceeded가 아니면 더 시도하지 않음
                    }
                }

                JsonNode items = root.get("items");
                if (items != null && items.isArray()) {
                    for (JsonNode item : items) {
                        String videoId = item.path("id").path("videoId").asText();
                        if (!videoId.isEmpty()) {
                            urls.add("https://www.youtube.com/watch?v=" + videoId);
                        }
                    }
                }

                // 성공했으면 반복 중지
                if (!urls.isEmpty()) {
                    break;
                }

            } catch (Exception e) {
                log.error("API 키 {}에서 예외 발생: {}", apiKey, e.getMessage());
            }
        }

        if (urls.isEmpty()) {
            log.error("모든 API 키에서 데이터를 가져오지 못했습니다.");
        }

        return urls;
    }

}
