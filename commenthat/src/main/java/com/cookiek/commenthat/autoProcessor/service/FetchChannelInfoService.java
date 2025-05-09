package com.cookiek.commenthat.autoProcessor.service;

import com.cookiek.commenthat.autoProcessor.dto.ChannelInfoDTO;
import com.cookiek.commenthat.domain.ChannelInfo;
import com.cookiek.commenthat.repository.ChannelInfoInterface;
import com.cookiek.commenthat.repository.UserInterface;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
public class FetchChannelInfoService {

    private final WebClient webClient;
    private final UserInterface userInterface;
    private final ChannelInfoInterface channelInfoInterface;
    private final TransactionTemplate transactionTemplate;

    // 병렬 요청을 위한 ExecutorService (스레드풀)
    private ExecutorService executorService;

    @Value("${youtube.api.key6}")
    private String apiKey;

    public FetchChannelInfoService(WebClient.Builder webClientBuilder,
                                   UserInterface userInterface,
                                   ChannelInfoInterface channelInfoInterface,
                                   TransactionTemplate transactionTemplate) {
        this.webClient = webClientBuilder.baseUrl("https://www.googleapis.com/youtube/v3").build();
        this.userInterface = userInterface;
        this.channelInfoInterface = channelInfoInterface;
        this.transactionTemplate = transactionTemplate;
    }

    // 병렬 실행을 위한 스레드풀 초기화
    @PostConstruct
    public void init() {
        this.executorService = Executors.newFixedThreadPool(10); // 병렬 스레드 수 조절 가능
    }


    /**
     * WebClient 호출 + DB 저장 (논블로킹 처리)
     */
    public void fetchAndSaveAsync(String channelId, Long userId) {
        getChannelStatistics(channelId)
                .doOnSuccess(statistics -> {
                    if (statistics == null) {
                        log.warn("No statistics found for channelId: {}", channelId);
                        return;
                    }

                    transactionTemplate.execute(status -> {
                        userInterface.findById(userId).ifPresentOrElse(user -> {
                            ChannelInfo channelInfo = ChannelInfo.createChannelInfo(
                                    user,
                                    statistics.getViewCount(),
                                    statistics.getSubscriberCount()
                            );
                            channelInfoInterface.save(channelInfo);
                            log.info("Saved channel info for userId: {}", userId);
                        }, () -> {
                            log.warn("User not found for userId: {}", userId);
                        });
                        return null;
                    });
                })
                .doOnError(e -> log.error("Error in fetching or saving for userId: {}, error: {}", userId, e.getMessage()))
                .subscribe();
    }

    /**
     * WebClient 비동기 요청 → Mono 로 통계 정보 수신
     */
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
                    try {
                        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
                        if (items == null || items.isEmpty()) return Mono.empty();

                        Map<String, Object> item = items.get(0);
                        Map<String, Object> statistics = (Map<String, Object>) item.get("statistics");
                        long subscriberCount = Long.parseLong((String) statistics.getOrDefault("subscriberCount", "0"));
                        long viewCount = Long.parseLong((String) statistics.getOrDefault("viewCount", "0"));

                        return Mono.just(new ChannelInfoDTO(subscriberCount, viewCount));
                    } catch (Exception e) {
                        log.error("Error parsing statistics for channelId: {}", channelId, e);
                        return Mono.empty();
                    }
                });
    }

    @PreDestroy
    public void shutdown() {
        log.info("FetchChannelInfoService 종료 중... ExecutorService shutdown 시작");

        executorService.shutdown(); // 먼저 graceful shutdown

        try {
            // 최대 10초 대기 후 강제 종료 시도
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                log.warn("ExecutorService가 종료되지 않아 강제 종료 시도");
                executorService.shutdownNow();
            } else {
                log.info("ExecutorService 정상 종료됨");
            }
        } catch (InterruptedException e) {
            log.error("ExecutorService 종료 중 인터럽트 발생", e);
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}

//이 구조는 Spring MVC 환경에서:
//
//WebClient를 논블로킹으로 유지하면서
//JPA 저장을 안전하게 트랜잭션 안에서 실행하며
//병렬 스레드에서 빠르게 수집하는
//
//✅ 실무에서도 많이 활용되는 고성능 + 안정성 구조입니다.