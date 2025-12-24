package com.example.demo.service.impl;

import com.example.demo.data.dto.ReviewDto;
import com.example.demo.data.dto.notification.ReviewWriteEvent;
import com.example.demo.data.dto.owner.OwnerReviewDto;
import com.example.demo.data.model.Diner;
import com.example.demo.data.model.Member;
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

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final DinerRepository dinerRepository;

    private final ApplicationEventPublisher eventPublisher;

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
        review.setRating(reviewDto.getRating());
        review.setComment(reviewDto.getComment());
        reviewRepository.save(review);
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
        Review review = Review.builder()
                .memberId(memberId)
                .bookId(reviewDto.getBookId())
                .dinerId(reviewDto.getDinerId())
                .rating(reviewDto.getRating())
                .comment(reviewDto.getComment())
                .build();
        String dinerName = diner.getDinerName();
        Review savedReview = reviewRepository.save(review);
        mapToReviewDto(savedReview, dinerName);

        // Owner Entity 들어오면 수정될수도 있음
        eventPublisher.publishEvent(new ReviewWriteEvent(
                diner.getOwner().getId(),
                diner.getDinerName()
        ));
    }

    //리뷰 삭제
    @Override
    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }

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



    @Override
    public Page<OwnerReviewDto> getOwnerReviews(Member owner, Long dinerId, int page, int size) {

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
}