package com.cookiek.commenthat.autoProcessor.service;

import com.cookiek.commenthat.autoProcessor.dto.ChannelInfoDTO;
import com.cookiek.commenthat.domain.ChannelInfo;
import com.cookiek.commenthat.domain.User;
import com.cookiek.commenthat.repository.ChannelInfoRepository;
import com.cookiek.commenthat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

//@Slf4j
//@Service
//@Transactional
//public class FetchInitialDataService {
//
//    private final WebClient webClient;
//
//    @Value("${youtube.api.key}")
//    private String apiKey;
//
//    public FetchInitialDataService(WebClient.Builder webClientBuilder) {
//        this.webClient = webClientBuilder.baseUrl("https://www.googleapis.com/youtube/v3").build();
//    }
//
//    @Async  // 메서드를 비동기로 실행
//    public CompletableFuture<ChannelInfoDTO> fetchChannelInfoAsync(String channelName) {
//
//        return getChannelId(channelName)
//                .flatMap(channelId -> getChannelStatistics(channelId)
//                        .map(statistics -> new ChannelInfoDTO(
//                                statistics.getViewCount(),
//                                statistics.getSubscriberCount())
//                        )
//                )
//                .doOnError(e -> log.error("Error occurred: ", e))
//                .toFuture(); // Mono를 CompletableFuture로 변환하여 ChannelInfo 반환
//    }
//
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
//
//    private Mono<ChannelInfoDTO> getChannelStatistics(String channelId) {
//        return webClient.get()
//                .uri(uriBuilder -> uriBuilder
//                        .path("/channels")
//                        .queryParam("part", "statistics")
//                        .queryParam("id", channelId)
//                        .queryParam("key", apiKey)
//                        .build())
//                .retrieve()
//                .bodyToMono(Map.class)
//                .flatMap(response -> {
//                    if (response.containsKey("items")) {
//                        Map<String, Object> item = ((List<Map<String, Object>>) response.get("items")).get(0);
//                        Map<String, Object> statistics = (Map<String, Object>) item.get("statistics");
//                        long subscriberCount = Long.parseLong((String) statistics.getOrDefault("subscriberCount", "0"));
//                        long viewCount = Long.parseLong((String) statistics.getOrDefault("viewCount", "0"));
//                        return Mono.just(new ChannelInfoDTO(subscriberCount, viewCount));
//                    }
//                    return Mono.empty();
//                });
//    }
//}
@Slf4j
@Service
//@RequiredArgsConstructor
public class FetchInitialDataService {

    private final WebClient webClient;
    private final ChannelInfoRepository channelInfoRepository;
    private final UserRepository userRepository;

    @Value("${youtube.api.key}")
    private String apiKey;

    public FetchInitialDataService(WebClient.Builder webClientBuilder, ChannelInfoRepository channelInfoRepository, UserRepository userRepository) {
        this.webClient = webClientBuilder.baseUrl("https://www.googleapis.com/youtube/v3").build();
        this.channelInfoRepository = channelInfoRepository;
        this.userRepository = userRepository;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW) // 새로운 트랜잭션을 시작하도록 설정
    public void fetchChannelInfoAsync(String channelName, Long userId) {
        try {
            String channelId = getChannelId(channelName).block(); // block()으로 값을 동기적으로 가져옴
            ChannelInfoDTO channelInfoDTO = getChannelStatistics(channelId).block();

            if (channelInfoDTO != null) {
                User user = userRepository.findById(userId);
                ChannelInfo channelInfo = ChannelInfo.createChannelInfo(user, channelInfoDTO.getViewCount(), channelInfoDTO.getSubscriberCount());
                channelInfoRepository.save(channelInfo);
            }
        } catch (Exception e) {
            log.error("Error fetching channel info", e);
        }
    }

    private Mono<String> getChannelId(String channelName) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("part", "snippet")
                        .queryParam("q", channelName)
                        .queryParam("type", "channel")
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(response -> {
                    if (response.containsKey("items")) {
                        Map<String, Object> item = ((List<Map<String, Object>>) response.get("items")).get(0);
                        Map<String, Object> idMap = (Map<String, Object>) item.get("id");
                        return Mono.justOrEmpty((String) idMap.get("channelId"));
                    }
                    return Mono.empty();
                });
    }

    private Mono<ChannelInfoDTO> getChannelStatistics(String channelId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/channels")
                        .queryParam("part", "statistics")
                        .queryParam("id", channelId)
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(response -> {
                    if (response.containsKey("items")) {
                        Map<String, Object> item = ((List<Map<String, Object>>) response.get("items")).get(0);
                        Map<String, Object> statistics = (Map<String, Object>) item.get("statistics");
                        long subscriberCount = Long.parseLong((String) statistics.getOrDefault("subscriberCount", "0"));
                        long viewCount = Long.parseLong((String) statistics.getOrDefault("viewCount", "0"));
                        return Mono.just(new ChannelInfoDTO(subscriberCount, viewCount));
                    }
                    return Mono.empty();
                });
    }
}
