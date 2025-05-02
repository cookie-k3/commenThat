package com.cookiek.commenthat.service;

import com.cookiek.commenthat.domain.SentiStat;
import com.cookiek.commenthat.domain.Video;
import com.cookiek.commenthat.dto.PositiveCommentDto;
import com.cookiek.commenthat.repository.SentiRepository;
import com.cookiek.commenthat.repository.SentiStatRepository;
import lombok.RequiredArgsConstructor;
import org.openkoreantext.processor.OpenKoreanTextProcessorJava;
import org.springframework.stereotype.Service;
import org.openkoreantext.processor.tokenizer.KoreanTokenizer.KoreanToken;
import scala.collection.Seq;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SentiService {

    private final SentiRepository sentiRepository;
    private final SentiStatRepository sentiStatRepository;

    // 긍/부정 댓글 수 조회 (DB에 저장된 결과가 있으면 가져오고, 없으면 계산 후 저장)
    public List<Long> getSentiCount(Long videoId) {
        List<SentiStat> stats = sentiStatRepository.findByVideoId(videoId);
        if (!stats.isEmpty()) {
            Long negative = stats.stream()
                    .filter(s -> s.getIsPositive() == 0)
                    .findFirst()
                    .map(SentiStat::getCount)
                    .orElse(0L);
            Long positive = stats.stream()
                    .filter(s -> s.getIsPositive() == 1)
                    .findFirst()
                    .map(SentiStat::getCount)
                    .orElse(0L);
            return List.of(negative, positive);
        } else {
            List<Long> result = sentiRepository.getSentiCountByVideoId(videoId);
            saveSentiStat(videoId, result.get(0), result.get(1), null);
            return result;
        }
    }

    // like count 반영한 버전
//    public List<PositiveCommentDto> getPositiveWord(Long videoId) {
//        List<PositiveCommentDto> positiveComments = sentiRepository.findPositiveCommentsByVideoId(videoId);
//
//        Map<String, Long> wordCount = new HashMap<>();
//
//        for (PositiveCommentDto commentDto : positiveComments) {
//            String comment = commentDto.getText();
//            Long likeCount = commentDto.getValue();
//
//            Arrays.stream(comment.split("\\s+"))
//                    .filter(word -> word.length() > 1)
//                    .forEach(word -> {
//                        wordCount.put(word, wordCount.getOrDefault(word, 0L) + likeCount);
//                    });
//        }
//
//        return wordCount.entrySet().stream()
//                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()) // value 기준 내림차순 정렬
//                .map(e -> new PositiveCommentDto(e.getKey(), e.getValue()))
//                .collect(Collectors.toList());
//    }

    // like count 반영 안한 버전 (형태소 분석 후 빈도 기반)

    // 불용어 목록
    private static final Set<String> STOP_WORDS = Set.of(
            "수", "것", "하다", "되다", "이다", "보다", "없다", "않다", "해보다", "있다", "안", "또", "더", "게",
            "해", "시", "그렇다", "중", "많이", "예", "일", "자다", "후", "나", "못", "거", "좀", "건", "데",
            "기", "나오다", "위", "제", "한", "및", "맞다", "이렇게", "더욱", "요", "다른", "고", "여기", "위해",
            "그냥", "듯", "돼다", "약", "그래도", "조금", "어떻다", "그래서", "왜", "뭐", "이렇다", "등", "관련",
            "자신", "과정", "간", "때", "등등", "사람", "중심", "상황", "이후", "이유", "내용", "같이", "문제",
            "부분", "정보", "방법", "중요하다", "사용", "통해", "위치", "상세", "가능", "필요", "확인", "소개",
            "활용", "이용", "생각", "도움", "예정", "정도", "경우", "정리", "방식", "시간", "여부", "바로", "어디",
            "대표", "추천", "주요",
            "ㅂ니다", "습니다", "합니다", "했어요", "네요", "나요", "죠", "구요", "겠어요", "ㄹ까", "되었어요",
            "되어서", "되며", "되었다", "하면서", "하면서도", "하면서는", "", "\n", "\n\n", "\n\n\n", "일본",
            "여행", "계획", "어서", "어요", "는데", "입니다", "해서", "준비", "세요", "ㄴ다", "같다", "어야",
            "지만", "아서", "다양하다", "싶다", "되어다", "가다", "들다", "진짜"
    );

    // 제외할 품사 목록
    private static final Set<String> UNWANTED_POS = Set.of(
            "Josa", "Determiner", "Conjunction", "PreEomi", "Eomi", "Suffix",
            "Punctuation", "Foreign", "Pronoun", "Number", "KoreanParticle",
            "Alpha", "Adverb"
    );

    // 긍정 댓글 키워드 추출 (DB에 저장된 키워드가 있으면 가져오고, 없으면 새로 분석 후 저장)
    public List<PositiveCommentDto> getPositiveWord(Long videoId) {
        List<SentiStat> stats = sentiStatRepository.findByVideoId(videoId);
        Optional<SentiStat> positiveStat = stats.stream()
                .filter(s -> s.getIsPositive() == 1 && s.getKeywords() != null)
                .findFirst();

        if (positiveStat.isPresent()) {
            return parseJsonToDtoList(positiveStat.get().getKeywords());
        }

        // 긍정 댓글 가져오기
        List<PositiveCommentDto> positiveComments = sentiRepository.findPositiveCommentsByVideoId(videoId);
        Map<String, Long> wordCount = new HashMap<>();

        for (PositiveCommentDto commentDto : positiveComments) {
            String comment = commentDto.getText();

            // 1. 정규화
            CharSequence normalized = OpenKoreanTextProcessorJava.normalize(comment);
            // 2. 형태소 분석
            Seq<KoreanToken> tokens = OpenKoreanTextProcessorJava.tokenize(normalized);
            // 3. 형태소 필터링 + 워드 추출
            List<String> words = OpenKoreanTextProcessorJava.tokensToJavaKoreanTokenList(tokens).stream()
                    .filter(token -> !UNWANTED_POS.contains(token.getPos().toString()))
                    .map(token -> token.getText().replaceAll("[^가-힣a-zA-Z0-9]", "").trim())
                    .filter(word -> word.length() > 1)
                    .filter(word -> !STOP_WORDS.contains(word))
                    .collect(Collectors.toList());

            // 워드 카운트 집계
            for (String word : words) {
                wordCount.put(word, wordCount.getOrDefault(word, 0L) + 1L);
            }
        }

        // 빈도수 기준 내림차순 정렬
        List<PositiveCommentDto> result = wordCount.entrySet().stream()
                .filter(e -> e.getKey() != null && !e.getKey().isBlank())
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(300)
                .map(e -> new PositiveCommentDto(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        // 분석 결과를 DB에 저장
        savePositiveKeywords(videoId, result);

        return result;
    }

    // 부정/긍정 댓글 수 + 긍정 키워드를 DB에 저장
    private void saveSentiStat(Long videoId, Long negativeCount, Long positiveCount, List<PositiveCommentDto> positiveCommentDtos) {
        Video video = new Video(videoId);

        SentiStat negative = new SentiStat();
        negative.setVideo(video);
        negative.setIsPositive(0);
        negative.setCount(negativeCount);
        sentiStatRepository.save(negative);

        SentiStat positive = new SentiStat();
        positive.setVideo(video);
        positive.setIsPositive(1);
        positive.setCount(positiveCount);
        if (positiveCommentDtos != null) {
            positive.setKeywords(convertDtoListToJson(positiveCommentDtos));
        }
        sentiStatRepository.save(positive);
    }

    // 긍정 키워드만 별도로 저장
    private void savePositiveKeywords(Long videoId, List<PositiveCommentDto> keywords) {
        List<SentiStat> stats = sentiStatRepository.findByVideoId(videoId);
        for (SentiStat s : stats) {
            if (s.getIsPositive() == 1) {
                s.setKeywords(convertDtoListToJson(keywords));
                sentiStatRepository.save(s);
                break;
            }
        }
    }

    // PositiveCommentDto 리스트를 JSON 문자열로 변환
    private String convertDtoListToJson(List<PositiveCommentDto> list) {
        return "[" + list.stream()
                .map(dto -> "{\"text\":\"" + dto.getText() + "\",\"value\":" + dto.getValue() + "}")
                .collect(Collectors.joining(",")) + "]";
    }

    // JSON 문자열을 PositiveCommentDto 리스트로 변환
    private List<PositiveCommentDto> parseJsonToDtoList(String json) {
        if (json == null || json.isBlank()) return List.of();
        json = json.replace("[", "").replace("]", "");
        String[] items = json.split("\\},\\{");
        List<PositiveCommentDto> result = new ArrayList<>();
        for (String item : items) {
            item = item.replace("{", "").replace("}", "");
            String[] fields = item.split(",");
            String text = fields[0].split(":")[1].replace("\"", "");
            Long value = Long.parseLong(fields[1].split(":")[1]);
            result.add(new PositiveCommentDto(text, value));
        }
        return result;
    }
}
