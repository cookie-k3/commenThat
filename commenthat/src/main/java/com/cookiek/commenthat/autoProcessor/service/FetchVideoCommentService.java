package com.cookiek.commenthat.autoProcessor.service;

import com.cookiek.commenthat.autoProcessor.dto.VideoCommentDTO;
import com.cookiek.commenthat.domain.Video;
import com.cookiek.commenthat.domain.VideoComment;
import com.cookiek.commenthat.repository.VideoInterface;
import com.cookiek.commenthat.repository.VideoCommentInterface;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class FetchVideoCommentService {

    private final WebClient webClient;
    private final ExecutorService executorService;
    private final TransactionTemplate transactionTemplate;
    private final VideoInterface videoInterface;
    private final VideoCommentInterface videoCommentInterface;

    @Value("${youtube.api.key1}")
    private String apiKey1;
    @Value("${youtube.api.key2}")
    private String apiKey2;
    @Value("${youtube.api.key3}")
    private String apiKey3;
    @Value("${youtube.api.key4}")
    private String apiKey4;

    private List<String> apiKeys;
    private final AtomicInteger keyPointer = new AtomicInteger(0);

    public FetchVideoCommentService(
            WebClient.Builder builder,
            ExecutorService executorService,
            TransactionTemplate transactionTemplate,
            VideoInterface videoInterface,
            VideoCommentInterface videoCommentInterface
    ) {
        this.webClient = builder.baseUrl("https://www.googleapis.com/youtube/v3").build();
        this.executorService = executorService;
        this.transactionTemplate = transactionTemplate;
        this.videoInterface = videoInterface;
        this.videoCommentInterface = videoCommentInterface;
    }

    @PostConstruct
    public void initApiKeys() {
        apiKeys = new ArrayList<>();
        if (apiKey1 != null) apiKeys.add(apiKey1);
        if (apiKey2 != null) apiKeys.add(apiKey2);
        if (apiKey3 != null) apiKeys.add(apiKey3);
        if (apiKey4 != null) apiKeys.add(apiKey4);
    }

    // Round-robin API key selector
    private String nextApiKey() {
        int idx = keyPointer.getAndUpdate(i -> (i + 1) % apiKeys.size());
        log.info("선택된 API 키: {}", idx + 1);
        return apiKeys.get(idx);
    }

    public void fetchVideoCommentsAsync(Long videoId) {
        log.info("댓글 수집 비동기 시작 - videoId={}", videoId);
        executorService.submit(() -> {
            transactionTemplate.executeWithoutResult(tx -> {
                Video video = videoInterface.findById(videoId).orElse(null);
                if (video == null || video.getVideoYoutubeId() == null) {
                    log.warn("Video or YouTube ID not found: videoId={}", videoId);
                    return;
                }
                String videoYoutubeId = video.getVideoYoutubeId();
//                log.info("YouTube ID 찾음 - videoId={}, videoYoutubeId={}", videoId, videoYoutubeId);

                List<VideoCommentDTO> collected = new ArrayList<>();
                fetchCommentsWithRetry(videoYoutubeId, null, collected);
            });
        });
    }

    public void fetchCurrentVideoCommentsAsync(Long videoId, LocalDateTime fromDate) {
        executorService.submit(() -> {
            transactionTemplate.executeWithoutResult(tx -> {
                Video video = videoInterface.findById(videoId).orElse(null);
                if (video == null || video.getVideoYoutubeId() == null) {
                    log.warn("Video or YouTube ID not found: videoId={}", videoId);
                    return;
                }
                String videoYoutubeId = video.getVideoYoutubeId();

                List<VideoCommentDTO> collected = new ArrayList<>();
                fetchCurrentCommentsWithRetry(videoYoutubeId, null, collected, fromDate);
            });
        });
    }

    private void fetchCommentsWithRetry(String videoYoutubeId,
                                        String pageToken,
                                        List<VideoCommentDTO> accumulator) {
        log.info("댓글 요청 - videoYoutubeId={}, pageToken={}", videoYoutubeId, pageToken);

        webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                            .path("/commentThreads")
                            .queryParam("part", "snippet")
                            .queryParam("videoId", videoYoutubeId)
                            .queryParam("key", nextApiKey())
                            .queryParam("maxResults", 100);
                    if (pageToken != null) builder.queryParam("pageToken", pageToken);
                    return builder.build();
                })
                .retrieve()
                .bodyToMono(Map.class)
                .flatMapMany(response -> {
//                    log.info("댓글 응답 수신 - videoYoutubeId={}", videoYoutubeId);
                    List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
                    if (items != null) {
//                        log.info("댓글 개수 수신됨 - videoYoutubeId={}, items.size={}", videoYoutubeId, items.size());
                        for (Map<String, Object> item : items) {
                            try {
                                Map<String, Object> snippet = (Map<String, Object>) ((Map<String, Object>) item.get("snippet")).get("topLevelComment");
                                Map<String, Object> topSnippet = (Map<String, Object>) snippet.get("snippet");

                                String rawText = (String) topSnippet.get("textDisplay");
                                Long likeCount = ((Number) topSnippet.getOrDefault("likeCount", 0)).longValue();
                                String publishedAtStr = (String) topSnippet.get("publishedAt");
                                ZonedDateTime zonedDateTime = ZonedDateTime.parse(publishedAtStr, DateTimeFormatter.ISO_DATE_TIME);
                                LocalDateTime publishedAt = zonedDateTime.withZoneSameInstant(ZoneId.of("Asia/Seoul")).toLocalDateTime();

                                String text = Jsoup.parse(rawText).text();

//                                log.info("댓글 파싱됨 - text='{}', likeCount={}, publishedAt={}", text, likeCount, publishedAt);

                                if (text != null && !text.trim().isEmpty()) {
                                    accumulator.add(new VideoCommentDTO(text, likeCount, publishedAt));
                                }
                            } catch (Exception e) {
                                log.warn("댓글 파싱 중 오류 발생: {}", e.getMessage(), e);
                            }
                        }
                    } else {
                        log.info("댓글 응답의 items가 null임 - videoYoutubeId={}", videoYoutubeId);
                    }

                    String nextPageToken = (String) response.get("nextPageToken");
                    if (nextPageToken != null) {
                        log.info("다음 페이지 존재 - videoYoutubeId={}, nextPageToken={}", videoYoutubeId, nextPageToken);
                        fetchCommentsWithRetry(videoYoutubeId, nextPageToken, accumulator);
                    } else {
                        log.info("모든 페이지 조회 완료 - videoYoutubeId={}", videoYoutubeId);
                        saveComments(accumulator, videoYoutubeId);
                    }

                    return Flux.empty();
                })
                .doOnError(error -> {
                    log.warn("API key error, next key: {}", error.getMessage());
                    fetchCommentsWithRetry(videoYoutubeId, pageToken, accumulator);
                })
                .subscribe();
    }

    private void fetchCurrentCommentsWithRetry(String videoYoutubeId,
                                               String pageToken,
                                               List<VideoCommentDTO> accumulator,
                                               LocalDateTime fromDate) {
        webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                            .path("/commentThreads")
                            .queryParam("part", "snippet")
                            .queryParam("videoId", videoYoutubeId)
                            .queryParam("key", nextApiKey())
                            .queryParam("maxResults", 100);
                    if (pageToken != null) builder.queryParam("pageToken", pageToken);
                    return builder.build();
                })
                .retrieve()
                .bodyToMono(Map.class)
                .flatMapMany(response -> {
                    List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
                    if (items != null) {
                        for (Map<String, Object> item : items) {
                            Map<String, Object> snippet = (Map<String, Object>) ((Map<String, Object>) item.get("snippet")).get("topLevelComment");
                            Map<String, Object> topSnippet = (Map<String, Object>) snippet.get("snippet");

                            String rawText = (String) topSnippet.get("textDisplay");
                            Long likeCount = ((Number) topSnippet.getOrDefault("likeCount", 0)).longValue();
                            String publishedAtStr = (String) topSnippet.get("publishedAt");
                            LocalDateTime publishedAt = ZonedDateTime.parse(publishedAtStr, DateTimeFormatter.ISO_DATE_TIME)
                                    .withZoneSameInstant(ZoneId.of("Asia/Seoul")).toLocalDateTime();

                            String text = Jsoup.parse(rawText).text();

                            if (text != null && !text.trim().isEmpty() && publishedAt.isAfter(fromDate)) {
                                accumulator.add(new VideoCommentDTO(text, likeCount, publishedAt));
                            }
                        }
                    }

                    String nextPageToken = (String) response.get("nextPageToken");
                    if (nextPageToken != null) {
                        fetchCurrentCommentsWithRetry(videoYoutubeId, nextPageToken, accumulator, fromDate);
                    } else {
                        saveComments(accumulator, videoYoutubeId);
                    }

                    return Flux.empty();
                })
                .doOnError(error -> {
                    log.warn("API key error, next key: {}", error.getMessage());
                    fetchCurrentCommentsWithRetry(videoYoutubeId, pageToken, accumulator, fromDate);
                })
                .subscribe();
    }

    private void saveComments(List<VideoCommentDTO> comments, String videoYoutubeId) {
//        log.info("댓글 저장 시도 - videoYoutubeId={}, 총 개수={}", videoYoutubeId, comments.size());
        Video video = videoInterface.findByVideoYoutubeId(videoYoutubeId);
        if (video == null) {
            log.warn("saveComments 실패 - videoYoutubeId={}에 해당하는 Video가 없음", videoYoutubeId);
            return;
        }

        for (VideoCommentDTO comment : comments) {
            VideoComment vc = new VideoComment();
            vc.setVideo(video);
            vc.setComment(comment.getComment());
            vc.setLikeCount(comment.getLikeCount());
            vc.setDate(comment.getDate());
            vc.setIsPositive(null);
            videoCommentInterface.save(vc);
//            log.info("댓글 저장됨 - videoId={}, comment={}", video.getId(), comment.getComment());
        }
        log.info("모든 댓글 저장 완료 - videoId={}, 총 저장된 댓글={}", video.getId(), comments.size());
    }

    @PreDestroy
    public void shutdown() {
        log.info("FetchVideoCommentService shutdown: terminating executor...");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
