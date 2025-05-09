package com.cookiek.commenthat.autoProcessor.service;

import com.cookiek.commenthat.autoProcessor.dto.VideoDTO;
import com.cookiek.commenthat.domain.User;
import com.cookiek.commenthat.domain.Video;
import com.cookiek.commenthat.repository.UserInterface;
import com.cookiek.commenthat.repository.VideoInterface;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FetchVideoService {

    private static final int MINUS_MONTH = 3;
    private final VideoInterface videoInterface;
    private final UserInterface userInterface;
    private final TransactionTemplate transactionTemplate;
    private final RestTemplate restTemplate;

    @Value("${youtube.api.key5}") private String apiKey5;
    @Value("${youtube.api.key6}") private String apiKey6;
    private List<String> apiKeys;
    private final AtomicInteger keyPointer = new AtomicInteger(0);

    public FetchVideoService(
            VideoInterface videoInterface,
            UserInterface userInterface,
            TransactionTemplate transactionTemplate,
            RestTemplate restTemplate  // ✅ RestTemplate 주입
    ) {
        this.videoInterface = videoInterface;
        this.userInterface = userInterface;
        this.transactionTemplate = transactionTemplate;
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void initApiKeys() {
        apiKeys = new ArrayList<>();
        if (apiKey5 != null) apiKeys.add(apiKey5);
        if (apiKey6 != null) apiKeys.add(apiKey6);
    }

    /** 라운드로빈으로 다음 API 키를 꺼내는 헬퍼 */
    private String nextApiKey() {
        int idx = keyPointer.getAndUpdate(i -> (i + 1) % apiKeys.size());
        return apiKeys.get(idx);
    }

    public void fetchVideosInit(String channelId, Long userId) {
        fetchAndSaveInit(channelId, userId);
    }

    public void fetchVideosByDate(String channelId, Long userId, LocalDateTime fromDate) {
        fetchAndSaveByDate(channelId, userId, fromDate);
    }

    private void fetchAndSaveInit(String channelId, Long userId) {
        try {
            List<String> videoIds = getVideoIdsInit(channelId);

            List<List<String>> partitionedLists = new ArrayList<>();
            for (int i = 0; i < videoIds.size(); i += 50) {
                partitionedLists.add(videoIds.subList(i, Math.min(i + 50, videoIds.size())));
            }

            List<VideoDTO> allVideoDetails = new ArrayList<>();
            for (List<String> batch : partitionedLists) {
                List<VideoDTO> videoDetails = getVideoDetails(batch);
                allVideoDetails.addAll(videoDetails);
            }

            transactionTemplate.executeWithoutResult(tx -> {
                User user = userInterface.findById(userId).orElse(null);
                if (user == null) {
                    log.warn("User not found for userId: {}", userId);
                    return;
                }

                for (VideoDTO dto : allVideoDetails) {
                    if (videoInterface.existsByVideoYoutubeId(dto.getVideoYoutubeId())) {
                        log.info("이미 저장된 영상 - videoYoutubeId: {}", dto.getVideoYoutubeId());
                        continue;
                    }
                    Video video = new Video();
                    video.setTitle(dto.getTitle());
                    video.setDescription(dto.getDescription());
                    video.setThumbnail(dto.getThumbnail());
                    video.setDate(dto.getUploadDate());
                    video.setVideoYoutubeId(dto.getVideoYoutubeId());
                    video.setUser(user);
                    videoInterface.save(video);

                }
                log.info("Saved {} videos for userId {}", allVideoDetails.size(), userId);
            });

        } catch (Exception e) {
            log.error("Error in saving videos: {}", e.getMessage(), e);
        }
    }

    private void fetchAndSaveByDate(String channelId, Long userId, LocalDateTime fromDate) {
        try {
            List<String> videoIds = getVideoIdsByDate(channelId, fromDate);

            List<List<String>> partitionedLists = new ArrayList<>();
            for (int i = 0; i < videoIds.size(); i += 50) {
                partitionedLists.add(videoIds.subList(i, Math.min(i + 50, videoIds.size())));
            }

            List<VideoDTO> allVideoDetails = new ArrayList<>();
            for (List<String> batch : partitionedLists) {
                List<VideoDTO> videoDetails = getVideoDetails(batch);
                allVideoDetails.addAll(videoDetails);
            }

            transactionTemplate.executeWithoutResult(tx -> {
                User user = userInterface.findById(userId).orElse(null);
                if (user == null) {
                    log.warn("User not found for userId: {}", userId);
                    return;
                }

                for (VideoDTO dto : allVideoDetails) {
                    if (videoInterface.existsByVideoYoutubeId(dto.getVideoYoutubeId())) {
                        log.info("이미 저장된 영상 - videoYoutubeId: {}", dto.getVideoYoutubeId());
                        continue;
                    }
                    Video video = new Video();
                    video.setTitle(dto.getTitle());
                    video.setDescription(dto.getDescription());
                    video.setThumbnail(dto.getThumbnail());
                    video.setDate(dto.getUploadDate());
                    video.setVideoYoutubeId(dto.getVideoYoutubeId());
                    video.setUser(user);
                    videoInterface.save(video);
                }
                log.info("Saved {} videos for userId {}", allVideoDetails.size(), userId);
            });

        } catch (Exception e) {
            log.error("Error in saving videos: {}", e.getMessage(), e);
        }
    }

    private List<String> getVideoIdsInit(String channelId) {
        String publishedAfter = toRfc3339(
                LocalDate.now().minusMonths(MINUS_MONTH).atStartOfDay());

        List<String> allVideoIds = new ArrayList<>();

        try {
            fetchVideoIdsRecursive(channelId, publishedAfter, null, allVideoIds);
        } catch (Exception e) {
            log.warn("getVideoIdsInit 오류 발생: {}", e.getMessage(), e);
        }

        return allVideoIds;
    }

    private List<String> getVideoIdsByDate(String channelId, LocalDateTime fromDate) {
        String publishedAfter = toRfc3339(fromDate);

        try {
            String url = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/youtube/v3/search")
                    .queryParam("key", nextApiKey())
                    .queryParam("channelId", channelId)
                    .queryParam("part", "snippet")
                    .queryParam("order", "date")
                    .queryParam("publishedAfter", publishedAfter)
                    .queryParam("maxResults", 50)
                    .queryParam("type", "video")
                    .build()
                    .toUriString();

            Map<String, Object> resp = restTemplate.getForObject(url, Map.class);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) resp.get("items");

            if (items == null) {
                return Collections.emptyList();
            }

            return items.stream()
                    .map(item -> ((Map<String, String>) item.get("id")).get("videoId"))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.warn("getVideoIdsByDate error: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private void fetchVideoIdsRecursive(String channelId,
                                        String publishedAfter,
                                        String pageToken,
                                        List<String> accumulator) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/youtube/v3/search")
                    .queryParam("key", nextApiKey())
                    .queryParam("channelId", channelId)
                    .queryParam("part", "snippet")
                    .queryParam("order", "date")
                    .queryParam("publishedAfter", publishedAfter)
                    .queryParam("maxResults", 50)
                    .queryParam("type", "video");

            if (pageToken != null) {
                builder.queryParam("pageToken", pageToken);
            }

            String url = builder.build().toUriString();


            Map<String, Object> resp = restTemplate.getForObject(url, Map.class);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) resp.get("items");
            if (items != null) {
                items.forEach(item -> accumulator.add(
                        ((Map<String, String>) item.get("id")).get("videoId")));
            }

            String next = (String) resp.get("nextPageToken");
            if (next != null && accumulator.size() < 500) {
                fetchVideoIdsRecursive(channelId, publishedAfter, next, accumulator);
            }

        } catch (Exception e) {
            log.warn("fetchVideoIdsRecursive error: {}", e.getMessage(), e);
        }
    }

    private List<VideoDTO> getVideoDetails(List<String> videoIds) {
        String ids = String.join(",", videoIds);

        try {
            String url = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/youtube/v3/videos")
                    .queryParam("key", nextApiKey())
                    .queryParam("id", ids)
                    .queryParam("part", "snippet,contentDetails")
                    .build()
                    .toUriString();

            Map<String, Object> resp = restTemplate.getForObject(url, Map.class);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) resp.get("items");

            if (items == null) {
                return Collections.emptyList();
            }

            return items.stream().map(item -> {
                        try {
                            Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");
                            Map<String, Object> contentDetails = (Map<String, Object>) item.get("contentDetails");

                            String duration = (String) contentDetails.get("duration");
                            if (isShorts(duration)) {
                                log.info("쇼츠 영상 제외됨 - videoId: {}, duration: {}", item.get("id"), duration);
                                return null;
                            }

                            Map<String, Object> thumbnails = (Map<String, Object>) snippet.get("thumbnails");
                            String thumbnailUrl = Optional.ofNullable((Map<String, Object>) thumbnails.get("maxres"))
                                    .map(m -> m.get("url").toString())
                                    .orElseGet(() -> Optional.ofNullable((Map<String, Object>) thumbnails.get("high"))
                                            .map(m -> m.get("url").toString()).orElse(null));

                            String publishedAtStr = (String) snippet.get("publishedAt");
                            LocalDateTime publishedAt = ZonedDateTime.parse(publishedAtStr,
                                            DateTimeFormatter.ISO_DATE_TIME)
                                    .withZoneSameInstant(ZoneId.of("Asia/Seoul"))
                                    .toLocalDateTime();

                            return new VideoDTO(
                                    (String) snippet.get("title"),
                                    (String) snippet.get("description"),
                                    publishedAt,
                                    thumbnailUrl,
                                    (String) item.get("id"));
                        } catch (Exception ex) {
                            log.warn("Failed to parse video item: {}", item, ex);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.warn("getVideoDetails error: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private boolean isShorts(String duration) {
        try {
            return Duration.parse(duration).getSeconds() <= 120;
        } catch (Exception e) {
            log.warn("Duration 파싱 실패: {}", duration, e);
            return false;
        }
    }

    private static String toRfc3339(LocalDateTime ldt) {
        return ldt.atZone(ZoneId.of("Asia/Seoul"))   // ① 애플리케이션 표준 타임존
                .withZoneSameInstant(ZoneOffset.UTC)
                .truncatedTo(ChronoUnit.SECONDS)   // ② 초 단위로 잘라 마이크로초 제거
                .format(DateTimeFormatter.ISO_INSTANT); // ③ 2025-02-08T15:00:00Z
    }
}
