package com.example.demo.service.impl;

import com.example.demo.common.python.KeywordService;
import com.example.demo.data.dto.ReviewDto;
import com.example.demo.data.dto.notification.ReviewWriteEvent;
import com.example.demo.data.dto.owner.OwnerReviewDto;
import com.example.demo.data.model.Diner;
import com.example.demo.data.model.Owner;
import com.example.demo.data.model.Review;
import com.example.demo.data.repository.DinerRepository;
import com.example.demo.data.repository.ReviewRepository;
import com.example.demo.service.ReviewService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final DinerRepository dinerRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final KeywordService keywordService;

    //식당페이지 리뷰 가져오기
    @Override
    public List<ReviewDto> getTop5Reviews(Long dinerId) {
        Diner diner = dinerRepository.findById(dinerId)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 식당이 없습니다."));
        String dinerName = diner.getDinerName();
        return reviewRepository.findTop5ByDinerIdOrderByCreateTimeDesc(dinerId)
                .stream().
                map(review -> mapToReviewDto(review, dinerName)).toList();
    }
    //마이페이지에서 내가 작성한 리뷰
    @Override
    public List<ReviewDto> getAllMyReview(Long memberId) {
        return reviewRepository.findByMemberId(memberId)
                .stream()
                .map(review -> {
                    String dinerName = dinerRepository.findById(review.getDinerId())
                            .map(Diner::getDinerName).orElse("알 수 없는 식당");
                    return mapToReviewDto(review, dinerName);
                })
                .toList();
    }

    //리뷰 수정 모달에 리뷰가져오기
    @Override
    public Optional<ReviewDto> getReview(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .map(review -> {
                    String dinerName = dinerRepository.findById(review.getDinerId())
                            .map(Diner::getDinerName)
                            .orElse("알 수 없는 식당");
                    return mapToReviewDto(review, dinerName);
                });
    }

    //리뷰 수정
    @Override
    @Transactional
    public void updateReview(Long reviewId, ReviewDto reviewDto) {
        Review review = reviewRepository.findById(reviewId).
                orElseThrow(() -> new IllegalArgumentException("해당하는 리뷰가 없습니다."));
        if(reviewDto.getComment().isEmpty()) {
            throw new IllegalArgumentException("리뷰 내용을 입력해주세요.");
        }
        if(reviewDto.getRating() == 0 || reviewDto.getRating() > 5) {
            throw new IllegalArgumentException("별점을 선택해주세요.");
        }
        if(reviewDto.getComment().replaceAll("\\s", "").length() > 100) {
            throw new IllegalArgumentException("리뷰는 100자 이내로 작성해주세요.");
        }
        review.setRating(reviewDto.getRating());
        review.setComment(reviewDto.getComment());

        // Entity : 키워드 수정 작업
        List<String> updatedKeywords = keywordService.getKeywordFromAi(reviewDto.getComment());
        review.setKeywords(updatedKeywords);
        reviewRepository.save(review);

        // for View : 키워드 업데이트
        updateDinerKeywords(review.getDinerId());

        String dinerName = dinerRepository.findById(review.getDinerId())
                        .map(Diner::getDinerName)
                        .orElse("알 수 없는 식당");
        mapToReviewDto(review, dinerName);
    }
    //리뷰 생성
    @Override
    @Transactional
    public void createReview(ReviewDto reviewDto, Long memberId) {
        Diner diner = dinerRepository.findById(reviewDto.getDinerId())
                .orElseThrow(() -> new IllegalArgumentException("해당하는 식당이 없습니다."));
        if(reviewRepository.findByBookId(reviewDto.getBookId()).isPresent()) {
            throw new IllegalArgumentException("이미 작성된 리뷰가 있습니다.");
        }
        if(reviewDto.getComment().isEmpty()) {
            throw new IllegalArgumentException("리뷰 내용을 입력해주세요.");
        }
        if(reviewDto.getRating() == 0) {
            throw new IllegalArgumentException("별점을 선택해주세요.");
        }
        if(reviewDto.getComment().replaceAll("\\s", "").length() > 100) {
            throw new IllegalArgumentException("리뷰는 100자 이내로 작성해주세요.");
        };

        // Entity : 키워드 추가 작업
        List<String> aiKeywords = keywordService.getKeywordFromAi(reviewDto.getComment());
        
        Review review = Review.builder()
                .memberId(memberId)
                .bookId(reviewDto.getBookId())
                .dinerId(reviewDto.getDinerId())
                .rating(reviewDto.getRating())
                .comment(reviewDto.getComment())
                .keywords(aiKeywords)
                .build();
        String dinerName = diner.getDinerName();
        Review savedReview = reviewRepository.save(review);

        // View : 키워드 업데이트
        updateDinerKeywords(reviewDto.getDinerId());

        mapToReviewDto(savedReview, dinerName);

        eventPublisher.publishEvent(new ReviewWriteEvent(
                diner.getOwner().getId(),
                diner.getDinerName()
        ));
    }

    //리뷰 삭제
    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 리뷰가 없습니다."));
        Long dinerId = review.getDinerId();

        reviewRepository.deleteById(reviewId);
        // 키워드 업데이트
        updateDinerKeywords(dinerId);
    }

    @Override
    public Page<OwnerReviewDto> getOwnerReviews(Owner owner, Long dinerId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());

        // 카테고리 선택
        if(dinerId != null) {
            return reviewRepository.findReviewByDinerId(dinerId, pageable);
        }
        // 미선택 (전체)
        List<Diner> diners = dinerRepository.findAllByOwner(owner);
        List<Long> dinerIds = diners.stream().map(Diner::getId).toList();

        if(dinerIds.isEmpty()) {
            return Page.empty();
        }
        return reviewRepository.findReviewByDinerIds(dinerIds, pageable);
    }

    //  Review -> ReviewDto 변환 메서드
    public ReviewDto mapToReviewDto(Review review, String dinerName) {
        return ReviewDto.builder()
                .reviewId(review.getReviewId())
                .memberId(review.getMemberId())
                .bookId(review.getBookId())
                .dinerId(review.getDinerId())
                .dinerName(dinerName)
                .rating(review.getRating())
                .comment(review.getComment())
                .createTime(review.getCreateTime())
                .updateTime(review.getUpdateTime())
                .build();
    }

    private void updateDinerKeywords(Long dinerId) {
        List<Review> reviews = reviewRepository.findByDinerId(dinerId);

        List<String> allCollectedTags = reviews.stream()
                .flatMap(r -> r.getKeywords().stream())
                .toList();

        List<String> top5 = allCollectedTags.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();

        dinerRepository.findById(dinerId).ifPresent(diner -> {
            diner.getKeywords().clear();
            diner.getKeywords().addAll(top5);
        });
    }
}