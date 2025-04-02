package com.cookiek.commenthat.autoProcessor.service;

import com.cookiek.commenthat.autoProcessor.dto.ChannelInfoDTO;
import com.cookiek.commenthat.domain.ChannelInfo;
import com.cookiek.commenthat.domain.User;
import com.cookiek.commenthat.repository.ChannelInfoInterface;
import com.cookiek.commenthat.repository.ChannelInfoRepository;
import com.cookiek.commenthat.repository.UserInterface;
import com.cookiek.commenthat.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.Hibernate;
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
import java.util.Optional;

@Slf4j
@Service
public class FetchInitialDataService {

    private final WebClient webClient;
    private final UserInterface userInterface;
    private final ChannelInfoInterface channelInfoInterface;

    @Value("${youtube.api.key}")
    private String apiKey;

    public FetchInitialDataService(WebClient.Builder webClientBuilder, UserInterface userInterface, ChannelInfoInterface channelInfoInterface) {
        this.webClient = webClientBuilder.baseUrl("https://www.googleapis.com/youtube/v3").build();
        this.userInterface = userInterface;
        this.channelInfoInterface = channelInfoInterface;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW) // 새로운 트랜잭션을 시작하도록 설정
    public void fetchChannelInfoAsync(String channelName, Long userId) {
        try {
            getChannelId(channelName)
                    .flatMap(channelId -> getChannelStatistics(channelId))
                    .flatMap(statistics -> userInterface.findById(userId)
                            .map(user -> {
                                ChannelInfo channelInfo = ChannelInfo.createChannelInfo(
                                        user,
                                        statistics.getViewCount(),
                                        statistics.getSubscriberCount()
                                );
                                return channelInfoInterface.save(channelInfo); // JPA save
                            })
                            .map(saved -> Mono.empty()) // 그냥 완료
                            .orElseGet(() -> {
                                log.warn("User not found: " + userId);
                                return Mono.empty();
                            })
                    )
                    .doOnError(e -> log.error("에러 발생!", e))
                    .subscribe();

//            if (channelInfoDTO != null) {
//                User user = userRepository.findById(userId);
//                ChannelInfo channelInfo = ChannelInfo.createChannelInfo(user, channelInfoDTO.getViewCount(), channelInfoDTO.getSubscriberCount());
//                channelInfoRepository.save(channelInfo);
//            }
        } catch (Exception e) {
            log.error("!!##== 에러 발생 ==##!! fetching channel info", e);
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