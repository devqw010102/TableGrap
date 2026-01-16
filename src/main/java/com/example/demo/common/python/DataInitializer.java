package com.example.demo.common.python;

import com.example.demo.data.model.Diner;
import com.example.demo.data.model.Review;
import com.example.demo.data.repository.DinerRepository;
import com.example.demo.data.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ReviewRepository reviewRepository;
    private final DinerRepository dinerRepository;
    private final KeywordService keywordService;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info(">>> [Batch] 데이터 보정 및 누적 빈도 분석 시작...");
        Thread.sleep(5000);

        List<Review> allReviews = reviewRepository.findAll();
        for (Review review : allReviews) {
            if (review.getKeywords().isEmpty()) {
                List<String> tags = keywordService.getKeywordFromAi(review.getComment());
                review.getKeywords().addAll(tags);
//                log.info("리뷰 ID {} 분석 완료: {}", review.getReviewId(), tags);
            }
        }

        List<Diner> allDiners = dinerRepository.findAll();
        for (Diner diner : allDiners) {
            List<Review> dinerReviews = reviewRepository.findByDinerId(diner.getId());

            List<String> allTagsForDiner = dinerReviews.stream()
                    .flatMap(r -> r.getKeywords().stream())
                    .toList();

            Map<String, Long> frequencyMap = allTagsForDiner.stream()
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

            List<String> top5 = frequencyMap.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(5)
                    .map(Map.Entry::getKey)
                    .toList();

            diner.getKeywords().clear();
            diner.getKeywords().addAll(top5);
//            log.info("식당 [{}] 누적 TOP 5 갱신 완료: {}", diner.getDinerName(), top5);
        }

        log.info(">>> [Batch] 모든 데이터 초기화 및 보정 완료.");
    }
}
