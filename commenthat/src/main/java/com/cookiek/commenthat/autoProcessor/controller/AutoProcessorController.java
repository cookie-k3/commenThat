package com.cookiek.commenthat.autoProcessor.controller;

import com.cookiek.commenthat.autoProcessor.service.*;
import com.cookiek.commenthat.domain.User;
import com.cookiek.commenthat.domain.Video;
import com.cookiek.commenthat.repository.UserInterface;
import com.cookiek.commenthat.repository.VideoCommentInterface;
import com.cookiek.commenthat.repository.VideoInterface;
import com.cookiek.commenthat.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private final FetchTopicUrlsService fetchTopicUrlsService;
    private final SentiService sentiService;

    private static final String LAST_MAX_ID_FILE = "last_max_user_id.txt";
    private final VideoCommentInterface videoCommentInterface;

    /**
     * 스케쥴링 코드
     **/

    // 모든 user의 channelInfo 업데이트
    @Scheduled(cron = "0 0 6 * * *") // 매일 06시 00분에 실행
    public void fetchChannelInfo() {
        List<User> users = userService.findAllUsers();
        for (User user : users) {
            String channelId = user.getChannelId();
            fetchChannelInfoService.fetchAndSaveAsync(channelId, user.getId());
        }
        log.info("스케줄러: 전체 사용자 채널 정보 업데이트 완료");
    }

    // 모든 VideoMeta 업데이트(제한없이)
    @Scheduled(cron = "0 1 6 * * *")
    public void fetchVideoMetaAll() {
        // 1. 모든 사용자 조회
        List<User> users = userService.findAllUsers();

        // 2. 사용자별로 영상 메타 처리
        for (User user : users) {
            Long userId = user.getId();
            String channelId = user.getChannelId();

            List<Long> videoIdList = videoInterface.findVideoIdsByUserId(userId);

            if (videoIdList.isEmpty()) {
                continue;
            }

            // 채널 ID → 구독자 수 조회
            Long subscriber = notSyncService.getSubscriberCount(channelId);

            // 비디오 메타 정보 저장
            fetchVideoMetaService.fetchVideosMetaAsync(videoIdList, subscriber);
        }

        log.info("자동 영상 메타 정보 업데이트 완료");
    }

    // 모든 유저의 새로운 영상 저장
    @Scheduled(cron = "0 2 6 * * ?") // 매일 6시에 2분에 실행
    public void fetchVideoAfter() {
        List<User> users = userService.findAllUsers();
        for (User user : users) {
            String channelId = user.getChannelId();
            LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
            fetchVideoService.fetchVideosByDate(channelId, user.getId(), oneDayAgo);
        }
        log.info("자동 영상 정보 업데이트 완료");
    }

    // 새로운 user가 추가될때 모든 영상과 댓글 수집  !!주의!! 단일서버에서만 잘 동작함. 여러 서버에서 동작하려면 db에 max_id 저장해야함
    @Scheduled(fixedDelay = 5000)   //5초마다 실행
    public void detectNewUserAndFetchComments() {
        long lastMaxId = readLastMaxId();
        long currentMaxId = userService.maxId();

        System.out.println("lastMaxId : " + lastMaxId + "  currentMaxId : " + currentMaxId);

        if (currentMaxId > lastMaxId) {
            log.info("새로운 유저 감지! lastMaxId={}, currentMaxId={}", lastMaxId, currentMaxId);

            List<User> users = userService.getGreaterThan(lastMaxId);
            if (users.isEmpty()) {
                log.warn("userId={} 보다 큰 수를 가진 유저가 없습니다.", lastMaxId);

            } else {
                for (User user : users) {
                    String channelId = notSyncService.getChannelId(user.getChannelName());
                    String channelImg = notSyncService.getChannelImg(user.getChannelName());

                    user.setChannelId(channelId);
                    user.setChannel_img(channelImg);
                    Long savedId = userService.updateUser(user);
                    System.out.println("update userId : " + savedId);

                    fetchVideoService.fetchVideosInit(user.getChannelId(), user.getId());
                }
            }

            List<Video> videos = videoInterface.findAllByUserIdGreaterThan(lastMaxId);
            for (Video video : videos) {
                fetchVideoCommentService.fetchVideoCommentsAsync(video.getId());
            }

            // 처리 완료 후 maxId 파일에 저장
            writeLastMaxId(currentMaxId);
        } else {
            log.debug("새 유저 없음. lastMaxId={}, currentMaxId={}", lastMaxId, currentMaxId);
        }
    }

    // 모든 영상의 새로운 댓글 수집하는 코드(제한 없음)
    @Scheduled(cron = "0 3 6 * * *")
//@Scheduled(cron = "0 13 * * * *")
    public void fetchRecentVideoComments() {
        List<Video> videos = videoInterface.findAll();

        for (Video video : videos) {
            // 각 영상의 가장 최신 댓글의 날짜 가져옴
            LocalDateTime date = videoCommentInterface.findLatestCommentDate(video.getId());

            log.info("최근 영상 댓글 처리 시작 - title={}, latesUploadDate={}", video.getTitle(), date);

            fetchVideoCommentService.fetchCurrentVideoCommentsAsync(video.getId(), date);
        }

        log.info("스케줄 완료 - 총 {}개의 영상 처리", videos.size());
    }

    // 모든 비디오 댓글의 긍부정 키워드 추출하는 코드 -> 이미 있으면 업데이트함
    @Scheduled(cron = "0 5 6 * * *")
    public void fetchPositiveComment() {
        List<Long> videoIds = videoInterface.findVideoIds();
        for (Long videoId : videoIds) {
            sentiService.getAndSavePositiveWords(videoId);
            log.info("videoId={} 긍부정 키워드 추출", videoId);
        }
    }


    /**
     * API 코드
     **/

    //http://localhost:8080/fetch-channel-info?userId=2
    @GetMapping("/fetch-channel-info")
    public Map<String, String> fetchChannelInfo(@RequestParam Long userId) {
        User user = userService.findUserById(userId);
        String channelId = user.getChannelId();
        fetchChannelInfoService.fetchAndSaveAsync(channelId, user.getId());

        Map<String, String> response = new HashMap<>();
        response.put("message", "success fetchChannelInfo");
        return response;
    }

    //http://localhost:8080/fetch-video-init?userId=2
//    @GetMapping("/fetch-video-init")

    /// /    @Transactional
//    //    @Scheduled(cron = "0 0 12 * * ?") // 매일 12시에 실행
//    public Map<String, String> fetchVideoInit(@RequestParam Long userId) {
//        User user = userService.findUserById(userId);
//        String channelId = user.getChannelId();
//        fetchVideoService.fetchVideosInitAsync(channelId, user.getId());
//
//        Map<String, String> response = new HashMap<>();
//        response.put("message", "success fetchVideoInit");
//        return response;
//    }

    //http://localhost:8080/fetch-video-after?userId=2
//    @GetMapping("/fetch-video-after")
//    public Map<String, String> fetchVideoAfter(@RequestParam Long userId) {
//        User user = userService.findUserById(userId);
//        String channelId = user.getChannelId();
//
//        // 예시 코드
//        LocalDateTime date = LocalDateTime.of(2025, 4, 10, 0, 0);
//        fetchVideoService.fetchVideosByDateAsync(channelId, user.getId(), date);
//
//        Map<String, String> response = new HashMap<>();
//        response.put("message", "success fetchVideoInit");
//        return response;
//    }

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

    // 한 영상에 대한 댓글만 가져옴
    //http://localhost:8080/fetch-video-comment?videoId=34
    @GetMapping("/fetch-video-comment")
    //    @Scheduled(cron = "0 0 12 * * ?") // 매일 12시에 실행
    public Map<String, String> fetchVideoComment(@RequestParam Long videoId) {
        fetchVideoCommentService.fetchVideoCommentsAsync(videoId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "success fetchVideoComment");
        return response;
    }

    // 한 유저의 모든 영상의 댓글 가져옴
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

    //http://localhost:8080/fetch-positive-comment?videoId=58
    @GetMapping("fetch-positive-comment")
    public Map<String, String> fetchPositiveComment(@RequestParam Long videoId) {
        sentiService.getAndSavePositiveWords(videoId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "success fetchPositiveComment");
        return response;
    }


    // -----------------------------이건 아직 자동화 안함-----------------------------

    //    //http://localhost:8080/fetch-topic-urls?contentsId=1
    @GetMapping("fetch-topic-urls")
    public Map<String, String> fetchTopicUrls(@RequestParam Long contentsId) {

        fetchTopicUrlsService.updateUrlsAsync(contentsId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "success fetchTopicUrls");
        return response;
    }

    // ------------------------------------------------------------------------------


    // 그 외에 util 관련 메소드

    private long readLastMaxId() {
        Path path = Paths.get(LAST_MAX_ID_FILE);
        try {
            if (Files.notExists(path)) {
                // 파일 없으면 생성 (초기값 0)
                Files.writeString(path, "0");
                log.info("last_max_user_id.txt 파일이 없어서 새로 생성했습니다.");
                return 0L;
            }
            String content = Files.readString(path).trim();
            return Long.parseLong(content);
        } catch (IOException | NumberFormatException e) {
            log.error("last_max_user_id.txt 읽기 실패, 기본값 0 사용", e);
            return 0L;
        }
    }

    private void writeLastMaxId(long maxId) {
        Path path = Paths.get(LAST_MAX_ID_FILE);
        try {
            Files.writeString(path, String.valueOf(maxId));
            log.info("last_max_user_id.txt에 {} 저장 완료", maxId);
        } catch (IOException e) {
            log.error("last_max_user_id.txt 저장 실패", e);
        }
    }


}
