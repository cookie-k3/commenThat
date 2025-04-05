package com.cookiek.commenthat.autoProcessor.controller;

import com.cookiek.commenthat.autoProcessor.service.*;
import com.cookiek.commenthat.domain.User;
import com.cookiek.commenthat.domain.Video;
import com.cookiek.commenthat.repository.UserInterface;
import com.cookiek.commenthat.repository.VideoInterface;
import com.cookiek.commenthat.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AutoProcessorController {

    private final UserService userService;
    private final NotSyncService notSyncService;
    private final VideoInterface videoInterface;
    private final FetchChannelInfoService fetchChannelInfoService;
    private final FetchVideoService fetchVideoService;
    private final FetchVideoMetaService fetchVideoMetaService;
    private final FetchVideoCommentService fetchVideoCommentService;

    //http://localhost:8080/fetch-channel-info?userId=2
    @GetMapping("/fetch-channel-info")
//    @Transactional
    //    @Scheduled(cron = "0 0 12 * * ?") // 매일 12시에 실행
    public Map<String, String> fetchChannelInfo(@RequestParam Long userId) {
        User user = userService.findUserById(userId);
        String channelId = user.getChannelId();
        fetchChannelInfoService.fetchAndSaveAsync(channelId, user.getId());

        Map<String, String> response = new HashMap<>();
        response.put("message", "success fetchChannelInfo");
        return response;
    }

    //http://localhost:8080/fetch-video-init?userId=2
    @GetMapping("/fetch-video-init")
//    @Transactional
    //    @Scheduled(cron = "0 0 12 * * ?") // 매일 12시에 실행
    public Map<String, String> fetchVideoInti(@RequestParam Long userId) {
        User user = userService.findUserById(userId);
        String channelId = user.getChannelId();
        fetchVideoService.fetchVideosInitAsync(channelId, user.getId());

        Map<String, String> response = new HashMap<>();
        response.put("message", "success fetchVideoInit");
        return response;
    }

    //http://localhost:8080/fetch-video-meta?userId=2
    @GetMapping("/fetch-video-meta")
    //    @Scheduled(cron = "0 0 12 * * ?") // 매일 12시에 실행
    public Map<String, String> fetchVideoMeta(@RequestParam Long userId) {
        // 1. 사용자 조회
        User user = userService.findUserById(userId);

        // 2. 최근 7일 이내 업로드된 영상 조회
        LocalDateTime sevenDaysAgo = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).minusDays(7).toLocalDateTime();
        List<Long> videoIdList = videoInterface.findByUserIdAndDateAfter(userId, sevenDaysAgo).stream()
                .map(Video::getId)
                .toList();

        if (videoIdList.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "최근 7일 이내 업로드된 영상이 없습니다.");
            return response;
        }

        // 3. 채널 ID → 구독자 수 조회
        Long subscriber = notSyncService.getSubscriberCount(user.getChannelId());

        // 4. 비디오 메타 정보 저장
        fetchVideoMetaService.fetchVideosMetaAsync(videoIdList, subscriber);

        Map<String, String> response = new HashMap<>();
        response.put("message", "success fetchVideoMeta");

        return response;
    }

    //http://localhost:8080/fetch-video-comment?videoId=34
    @GetMapping("/fetch-video-comment")
    //    @Scheduled(cron = "0 0 12 * * ?") // 매일 12시에 실행
    public Map<String, String> fetchVideoComment(@RequestParam Long videoId) {
        fetchVideoCommentService.fetchVideoCommentsAsync(videoId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "success fetchVideoComment");
        return response;
    }

    //http://localhost:8080/fetch-video-comment-all?userId=2
    @GetMapping("/fetch-video-comment-all")
// @Scheduled(cron = "0 0 12 * * ?")
    public Map<String, String> fetchVideoCommentAll(@RequestParam Long userId) {
        List<Video> videos = videoInterface.findAllByUserId(userId);

        if (videos.isEmpty()) {
            log.warn("userId={} 에 해당하는 영상이 없습니다.", userId);
        }

        for (Video video : videos) {
            log.info("댓글 수집 시작 - videoId={}, title={}, uploadDate={}", video.getId(), video.getTitle(), video.getDate());
            fetchVideoCommentService.fetchVideoCommentsAsync(video.getId());
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "success fetchVideoComment");
        response.put("userId", String.valueOf(userId));
        response.put("videoCount", String.valueOf(videos.size()));
        return response;
    }





}
