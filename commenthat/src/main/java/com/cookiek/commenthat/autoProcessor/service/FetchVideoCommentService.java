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
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// 대댓글은 안가져오고 탑댓글만 가져오는 코드 (오류가 안생김)
@Slf4j
@Service
public class FetchVideoCommentService {

    private final WebClient webClient;
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    private final TransactionTemplate transactionTemplate;
    private final VideoInterface videoInterface;
    private final VideoCommentInterface videoCommentInterface;

    @Value("${youtube.api.key}")
    private String apiKey1;

    @Value("${youtube.api.key2}")
    private String apiKey2;

    private List<String> apiKeys;

    public FetchVideoCommentService(
            WebClient.Builder builder,
            TransactionTemplate transactionTemplate,
            VideoInterface videoInterface,
            VideoCommentInterface videoCommentInterface
    ) {
        this.webClient = builder.baseUrl("https://www.googleapis.com/youtube/v3").build();
        this.transactionTemplate = transactionTemplate;
        this.videoInterface = videoInterface;
        this.videoCommentInterface = videoCommentInterface;
    }

    @PostConstruct
    public void initApiKeys() {
        apiKeys = new ArrayList<>();
        if (apiKey1 != null) apiKeys.add(apiKey1);
        if (apiKey2 != null) apiKeys.add(apiKey2);
    }

    public void fetchVideoCommentsAsync(Long videoId) {
        executorService.submit(() -> {
            transactionTemplate.executeWithoutResult(tx -> {
                Video video = videoInterface.findById(videoId).orElse(null);
                if (video == null || video.getVideoYoutubeId() == null) {
                    log.warn("Video or YouTube ID not found: videoId={}", videoId);
                    return;
                }
                String videoYoutubeId = video.getVideoYoutubeId();

                List<VideoCommentDTO> collected = new ArrayList<>();
                fetchCommentsWithRetry(videoYoutubeId, null, collected, 1);

            });
        });
    }

    private void fetchCommentsWithRetry(String videoYoutubeId, String pageToken, List<VideoCommentDTO> accumulator, int keyIndex) {
        if (keyIndex >= apiKeys.size()) {
            log.error("모든 API 키가 소진되어 더 이상 요청할 수 없습니다.");
            saveComments(accumulator, videoYoutubeId);
            return;
        }

        webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                            .path("/commentThreads")
                            .queryParam("part", "snippet")
                            .queryParam("videoId", videoYoutubeId)
                            .queryParam("key", apiKeys.get(keyIndex))
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
                            LocalDateTime publishedAt = LocalDateTime.parse(publishedAtStr, DateTimeFormatter.ISO_DATE_TIME);

                            // HTML 태그 제거
                            String text = Jsoup.parse(rawText).text();  // ← 이 부분 추가

                            // 댓글 공백 제거
                            if (text != null && !text.trim().isEmpty()) {
                                accumulator.add(new VideoCommentDTO(text, likeCount, publishedAt));
                            }

                        }
                    }

                    String nextPageToken = (String) response.get("nextPageToken");
                    if (nextPageToken != null) {
                        fetchCommentsWithRetry(videoYoutubeId, nextPageToken, accumulator, keyIndex);
                    } else {
                        saveComments(accumulator, videoYoutubeId);
                    }

                    return Flux.empty();
                })
                .doOnError(error -> {
                    log.warn("API 키 {} 오류 발생, 다음 키로 재시도: {}", keyIndex, error.getMessage());
                    fetchCommentsWithRetry(videoYoutubeId, pageToken, accumulator, keyIndex + 1);
                })
                .subscribe();
    }

    private void saveComments(List<VideoCommentDTO> comments, String videoYoutubeId) {
        Video video = videoInterface.findByVideoYoutubeId(videoYoutubeId);
        if (video == null) return;

        for (VideoCommentDTO comment : comments) {
            VideoComment vc = new VideoComment();
            vc.setVideo(video);
            vc.setComment(comment.getComment());
            vc.setLikeCount(comment.getLikeCount());
            vc.setDate(comment.getDate());
            vc.setIsPositive(null);
            videoCommentInterface.save(vc);
        }
        log.info("Saved {} comments for videoId {}", comments.size(), video.getId());
    }


    @PreDestroy
    public void shutdown() {
        log.info("FetchVideoCommentService 종료 중... ExecutorService shutdown 시작");

        executorService.shutdown();
        try {
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
