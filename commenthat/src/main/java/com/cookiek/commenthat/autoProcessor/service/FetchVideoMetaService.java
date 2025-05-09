package com.cookiek.commenthat.autoProcessor.service;

import com.cookiek.commenthat.autoProcessor.dto.VideoDTO;
import com.cookiek.commenthat.autoProcessor.dto.VideoMetaDTO;
import com.cookiek.commenthat.domain.Video;
import com.cookiek.commenthat.domain.VideoMeta;
import com.cookiek.commenthat.repository.UserInterface;
import com.cookiek.commenthat.repository.VideoInterface;
import com.cookiek.commenthat.repository.VideoMetaInterface;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class FetchVideoMetaService {

    private final WebClient webClient;
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    private final TransactionTemplate transactionTemplate;
    private final VideoInterface videoInterface;
    private final VideoMetaInterface videoMetaInterface;

    @Value("${youtube.api.key5}")
    private String apiKey;

    public FetchVideoMetaService(
            WebClient.Builder builder,
            TransactionTemplate transactionTemplate,
            VideoInterface videoInterface,
            VideoMetaInterface videoMetaInterface
    ) {
        this.webClient = builder.baseUrl("https://www.googleapis.com/youtube/v3").build();
        this.transactionTemplate = transactionTemplate;
        this.videoInterface = videoInterface;
        this.videoMetaInterface = videoMetaInterface;
    }

    public void fetchVideosMetaAsync(List<Long> videoIdList, Long subscriberCount) {
        LocalDateTime localDateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDateTime();
        executorService.submit(() -> {
            for (Long videoId : videoIdList) {
                transactionTemplate.executeWithoutResult(tx -> {
                    Video video = videoInterface.findById(videoId).orElse(null);
                    if (video == null) {
                        log.warn("Video not found: videoId={}", videoId);
                        return;
                    }

                    String videoYoutubeId = video.getVideoYoutubeId();
                    fetchVideoStatistics(videoYoutubeId)
                            .doOnSuccess(meta -> {
                                if (meta != null) {
                                    VideoMeta videoMeta = new VideoMeta();
                                    videoMeta.setVideo(video);
                                    videoMeta.setDate(localDateTime);
                                    videoMeta.setViews(meta.getViews());
                                    videoMeta.setLikes(meta.getLikes());
                                    videoMeta.setCommentCount(meta.getCommentCount());
                                    videoMeta.setSubscriber(subscriberCount);

                                    videoMetaInterface.save(videoMeta);
                                    log.info("VideoMeta 저장 완료: videoId={}, 조회수={}, 좋아요={}, 댓글수={}",
                                            videoId, meta.getViews(), meta.getLikes(), meta.getCommentCount());
                                } else {
                                    log.warn("Meta 정보 없음: videoId={}", videoId);
                                }
                            })
                            .doOnError(e -> log.error("Meta 수집 실패: videoId={}, error={}", videoId, e.getMessage(), e))
                            .subscribe();
                });
            }
        });
    }

    public Mono<VideoMetaDTO> fetchVideoStatistics(String videoYoutubeId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/videos")
                        .queryParam("key", apiKey)
                        .queryParam("id", videoYoutubeId)
                        .queryParam("part", "statistics")
                        .build())
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class).flatMap(errorBody -> {
                            log.error("YouTube API 오류 응답 (fetchVideoStatistics): {}", errorBody);
                            return Mono.error(new RuntimeException("YouTube API 호출 실패: " + errorBody));
                        }))
                .bodyToMono(Map.class)
                .flatMap(response -> {
                    List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
                    if (items == null || items.isEmpty()) {
                        return Mono.empty(); // ✅ null 대신 Mono.empty()
                    }

                    Map<String, Object> statistics = (Map<String, Object>) items.get(0).get("statistics");

                    long viewCount = Long.parseLong((String) statistics.getOrDefault("viewCount", "0"));
                    long likeCount = Long.parseLong((String) statistics.getOrDefault("likeCount", "0"));
                    long commentCount = Long.parseLong((String) statistics.getOrDefault("commentCount", "0"));

                    return Mono.just(new VideoMetaDTO(viewCount, likeCount, commentCount));
                });
    }


    @PreDestroy
    public void shutdown() {
        log.info("FetchVideoMetaService 종료 중... ExecutorService shutdown 시작");

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
