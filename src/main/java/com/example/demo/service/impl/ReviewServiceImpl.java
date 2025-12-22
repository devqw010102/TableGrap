package com.example.demo.service.impl;

import com.example.demo.data.dto.ReviewDto;
import com.example.demo.data.model.Review;
import com.example.demo.data.repository.ReviewRepository;
import com.example.demo.service.ReviewService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    //식당페이지 리뷰 가져오기
    @Override
    public List<ReviewDto> getTop5Reviews(Long dinerId) {
        return reviewRepository.findTop5ByDinerIdOrderByCreateTimeDesc(dinerId).stream().map(this::mapToReviewDto).toList();
    }
    //마이페이지에서 내가 작성한 리뷰
    @Override
    public List<ReviewDto> getAllMyReview(Long memberId) {
        return reviewRepository.findByMemberId(memberId).stream().map(this::mapToReviewDto).toList();
    }
    //리뷰 수정 모달에 리뷰가져오기
    @Override
    public Optional<ReviewDto> getReview(Long bookId) {
        return reviewRepository.findByBookId(bookId).stream().map(this::mapToReviewDto).toList().stream().findFirst();
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
        mapToReviewDto(review);
    }
    //리뷰 생성
    @Override
    @Transactional
    public void createReview(ReviewDto reviewDto, Long memberId) {
        Review review = Review.builder()
                .memberId(memberId)
                .bookId(reviewDto.getBookId())
                .dinerId(reviewDto.getDinerId())
                .dinerName(reviewDto.getDinerName())
                .rating(reviewDto.getRating())
                .comment(reviewDto.getComment())
                .build();

        Review savedReview = reviewRepository.save(review);
        mapToReviewDto(savedReview);
    }

    //리뷰 삭제
    @Override
    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }

    public ReviewDto mapToReviewDto(Review review) {
        return ReviewDto.builder()
                .reviewId(review.getReviewId())
                .memberId(review.getMemberId())
                .bookId(review.getBookId())
                .dinerId(review.getDinerId())
                .dinerName(review.getDinerName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createTime(review.getCreateTime())
                .build();
    }
}