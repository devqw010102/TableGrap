package com.example.demo.service.impl;

import com.example.demo.data.dto.ReviewDto;
import com.example.demo.data.model.Review;
import com.example.demo.data.repository.ReviewRepository;
import com.example.demo.service.ReviewService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;

    @Override
    public List<ReviewDto> getTop5Reviews(Long dinerId) {
        return reviewRepository.findTop5ByDinerIdOrderByCreateTimeDesc(dinerId).stream().map(this::mapToReviewDto).toList();
    }

    @Override
    public List<ReviewDto> getAllMyReview(Long memberId) {
        return reviewRepository.findByMemberId(memberId).stream().map(this::mapToReviewDto).toList();
    }


    @Override
    @Transactional
    public void createReview(ReviewDto reviewDto, Long memberId) {
        Review review = Review.builder()
                .memberId(memberId)
                .dinerId(reviewDto.getDinerId())
                .dinerName(reviewDto.getDinerName())
                .rating(reviewDto.getRating())
                .comment(reviewDto.getComment())
                .build();

        Review savedReview = reviewRepository.save(review);
        mapToReviewDto(savedReview);
    }

    public ReviewDto mapToReviewDto(Review review) {
        return ReviewDto.builder()
                .reviewId(review.getReviewId())
                .memberId(review.getMemberId())
                .dinerId(review.getDinerId())
                .dinerName(review.getDinerName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createTime(review.getCreateTime())
                .build();
    }
}