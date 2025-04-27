package com.cookiek.commenthat.autoProcessor.service;

import com.cookiek.commenthat.autoProcessor.dto.VideoDTO;
import com.cookiek.commenthat.domain.User;
import com.cookiek.commenthat.domain.Video;
import com.cookiek.commenthat.repository.UserInterface;
import com.cookiek.commenthat.repository.VideoInterface;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;


import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FetchVideoService {

    final int MINUS_MONTH = 3;

    private final WebClient webClient;
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    private final VideoInterface videoInterface;
    private final UserInterface userInterface;
    private final TransactionTemplate transactionTemplate;

    @Value("${youtube.api.key}")
    private String apiKey;

    public FetchVideoService(
            WebClient.Builder builder,
            VideoInterface videoInterface,
            UserInterface userInterface,
            TransactionTemplate transactionTemplate
    ) {
        this.webClient = builder.baseUrl("https://www.googleapis.com/youtube/v3").build();
        this.videoInterface = videoInterface;
        this.userInterface = userInterface;
        this.transactionTemplate = transactionTemplate;
    }

    public void fetchVideosInitAsync(String channelId, Long userId) {
        executorService.submit(() -> fetchAndSaveInit(channelId, userId));
    }

    public void fetchVideosByDateAsync(String channelId, Long userId, LocalDateTime fromDate) {
        executorService.submit(() -> fetchAndSaveByDate(channelId, userId, fromDate));
    }

    private void fetchAndSaveInit(String channelId, Long userId) {
        getVideoIdsInit(channelId)
                .flatMapMany(Flux::fromIterable)
                .buffer(50)
                .flatMap(list -> getVideoDetails(list).flatMapMany(Flux::fromIterable))  // Flux<VideoDTO>
                .collectList()
                .doOnSuccess(videoList -> {
                    transactionTemplate.executeWithoutResult(tx -> {
                        User user = userInterface.findById(userId).orElse(null);
                        if (user == null) {
                            log.warn("User not found for userId: {}", userId);
                            return;
                        }

                        for (VideoDTO dto : videoList) {
                            Video video = new Video();
                            video.setTitle(dto.getTitle());
                            video.setDescription(dto.getDescription());
                            video.setThumbnail(dto.getThumbnail());
                            video.setDate(dto.getUploadDate()); // 이미 LocalDateTime 형태
                            video.setVideoYoutubeId(dto.getVideoYoutubeId());
                            video.setUser(user);
                            videoInterface.save(video);
                        }
                        log.info("Saved {} videos for userId {}", videoList.size(), userId);
                    });
                })
                .doOnError(e -> log.error("Error in saving videos: {}", e.getMessage(), e))
                .subscribe();
    }

    private void fetchAndSaveByDate(String channelId, Long userId, LocalDateTime fromDate) {
        getVideoIdsByDate(channelId, fromDate)
                .flatMapMany(Flux::fromIterable)
                .buffer(50)
                .flatMap(list -> getVideoDetails(list).flatMapMany(Flux::fromIterable))  // Flux<VideoDTO>
                .collectList()
                .doOnSuccess(videoList -> {
                    transactionTemplate.executeWithoutResult(tx -> {
                        User user = userInterface.findById(userId).orElse(null);
                        if (user == null) {
                            log.warn("User not found for userId: {}", userId);
                            return;
                        }

                        for (VideoDTO dto : videoList) {
                            Video video = new Video();
                            video.setTitle(dto.getTitle());
                            video.setDescription(dto.getDescription());
                            video.setThumbnail(dto.getThumbnail());
                            video.setDate(dto.getUploadDate()); // 이미 LocalDateTime 형태
                            video.setVideoYoutubeId(dto.getVideoYoutubeId());
                            video.setUser(user);
                            videoInterface.save(video);
                        }
                        log.info("Saved {} videos for userId {}", videoList.size(), userId);
                    });
                })
                .doOnError(e -> log.error("Error in saving videos: {}", e.getMessage(), e))
                .subscribe();
    }

    private Mono<List<String>> getVideoIdsInit(String channelId) {
        String publishedAfter = LocalDate.now().minusMonths(MINUS_MONTH).atStartOfDay()
                .toInstant(ZoneOffset.UTC).toString();

        List<String> allVideoIds = new ArrayList<>();

        return fetchVideoIdsRecursive(channelId, publishedAfter, null, allVideoIds)
                .onErrorResume(e -> {
                    log.warn("getVideoIdsInit 도중 오류 발생, 누적된 데이터 반환: {}건", allVideoIds.size());
                    return Mono.empty(); // 오류 발생 시 처리
                })
                .then(Mono.just(allVideoIds));
    }

    private Mono<List<String>> getVideoIdsByDate(String channelId, LocalDateTime fromDate) {
        // 입력받은 시각 +1초 → 그 이후 영상 조회
        String publishedAfter = fromDate
                .plusSeconds(1)
                .toInstant(ZoneOffset.UTC)
                .toString();

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("key", apiKey)
                        .queryParam("channelId", channelId)
                        .queryParam("part", "id")
                        .queryParam("order", "date")
                        .queryParam("publishedAfter", publishedAfter)
                        .queryParam("maxResults", 50)
                        .queryParam("type", "video")
                        .build())
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class).flatMap(errorBody -> {
                            log.error("YouTube API 오류 응답 (getVideoIdsByDate): {}", errorBody);
                            return Mono.error(new RuntimeException("YouTube API 호출 실패: " + errorBody));
                        }))
                .bodyToMono(Map.class)
                .map(response -> {
                    List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
                    if (items == null) return List.of();
                    return items.stream()
                            .map(item -> ((Map<String, String>) item.get("id")).get("videoId"))
                            .collect(Collectors.toList());
                });
    }

    private Mono<Void> fetchVideoIdsRecursive(String channelId, String publishedAfter, String pageToken, List<String> accumulator) {
        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                            .path("/search")
                            .queryParam("key", apiKey)
                            .queryParam("channelId", channelId)
                            .queryParam("part", "id")
                            .queryParam("order", "date")
                            .queryParam("publishedAfter", publishedAfter)
                            .queryParam("maxResults", 50)
                            .queryParam("type", "video");
                    if (pageToken != null) {
                        builder.queryParam("pageToken", pageToken);
                    }
                    return builder.build();
                })
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class).flatMap(errorBody -> {
                            log.error("YouTube API 오류 응답 (fetchVideoIdsRecursive): {}", errorBody);
                            return Mono.error(new RuntimeException("YouTube API 호출 실패: " + errorBody));
                        }))
                .bodyToMono(Map.class)
                .flatMap(response -> {
                    try {
                        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
                        if (items != null) {
                            items.stream()
                                    .map(item -> ((Map<String, String>) item.get("id")).get("videoId"))
                                    .forEach(accumulator::add);
                        }
                        String nextPageToken = (String) response.get("nextPageToken");
                        if (nextPageToken != null && accumulator.size() < 500) { // 안전장치로 최대 500개 제한
                            return fetchVideoIdsRecursive(channelId, publishedAfter, nextPageToken, accumulator);
                        } else {
                            return Mono.empty();
                        }
                    } catch (Exception e) {
                        log.error("API 응답 처리 중 오류 발생: {}", e.getMessage(), e);
                        return Mono.error(e);
                    }
                });
    }

    private Mono<List<VideoDTO>> getVideoDetails(List<String> videoIds) {
        String ids = String.join(",", videoIds);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/videos")
                        .queryParam("key", apiKey)
                        .queryParam("id", ids)
                        .queryParam("part", "snippet,contentDetails") // duration 포함
                        .build())
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class).flatMap(errorBody -> {
                            log.error("YouTube API 오류 응답 (getVideoDetails): {}", errorBody);
                            return Mono.error(new RuntimeException("YouTube API 호출 실패: " + errorBody));
                        }))
                .bodyToMono(Map.class)
                .map(response -> {
                    List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
                    if (items == null) return List.of();

                    return items.stream().map(item -> {
                        try {
                            String videoId = (String) item.get("id");
                            Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");
                            Map<String, Object> thumbnails = (Map<String, Object>) snippet.get("thumbnails");

                            // ✅ 고화질 써버내일 선택
                            String thumbnailUrl = null;
                            if (thumbnails.get("maxres") != null) {
                                thumbnailUrl = (String) ((Map<String, Object>) thumbnails.get("maxres")).get("url");
                            } else if (thumbnails.get("high") != null) {
                                thumbnailUrl = (String) ((Map<String, Object>) thumbnails.get("high")).get("url");
                            } else if (thumbnails.get("medium") != null) {
                                thumbnailUrl = (String) ((Map<String, Object>) thumbnails.get("medium")).get("url");
                            } else if (thumbnails.get("default") != null) {
                                thumbnailUrl = (String) ((Map<String, Object>) thumbnails.get("default")).get("url");
                            }

                            // ✅ Shorts 체제 (독일 영상 개발 모델) - → 2분 (<= 120초) 이하 제외
                            Map<String, Object> contentDetails = (Map<String, Object>) item.get("contentDetails");
                            String duration = (String) contentDetails.get("duration");
                            if (isShorts(duration)) {
                                log.info("Skip short video (<2min): {} [{}]", snippet.get("title"), duration);
                                return null;
                            }

                            String publishedAtStr = (String) snippet.get("publishedAt");
                            LocalDateTime publishedAt = LocalDateTime.parse(publishedAtStr, DateTimeFormatter.ISO_DATE_TIME);

                            return new VideoDTO(
                                    (String) snippet.get("title"),
                                    (String) snippet.get("description"),
                                    publishedAt,
                                    thumbnailUrl,
                                    videoId
                            );
                        } catch (Exception e) {
                            log.warn("Failed to parse video item: {}", item, e);
                            return null;
                        }
                    }).filter(dto -> dto != null).collect(Collectors.toList());
                });
    }

    private boolean isShorts(String duration) {
        try {
            // ISO-8601 Duration 문자열을 java.time.Duration으로 직접 파싱
            Duration dur = Duration.parse(duration);

            return dur.getSeconds() <= 120; // 2분 이하이면 Shorts로 간주
        } catch (Exception e) {
            log.warn("Duration 파싱 실패: {}", duration);
            return false;
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("FetchVideoService 종료 중... ExecutorService shutdown 시작");

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
