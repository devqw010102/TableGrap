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
    public List<ReviewDto> getTop5Reviews(Long dinerId, Long memberId) {
        return reviewRepository.findTop5ByDinerIdAndMemberId(dinerId, memberId).stream().map(this::mapToReviewDto).toList();
    }

    @Override
    public List<ReviewDto> getAllMyReview(Long memberId) {
        return reviewRepository.findByMemberId(memberId).stream().map(this::mapToReviewDto).toList();
    }


    @Override
    @Transactional
    public ReviewDto createReview(ReviewDto reviewDto, Long memberId) {
        Review review = Review.builder()
                .memberId(memberId)
                .dinerId(reviewDto.getDinerId())
                .rating(reviewDto.getRating())
                .comment(reviewDto.getComment())
                .build();

        Review savedReview = reviewRepository.save(review);
        return mapToReviewDto(savedReview);
    }

    public ReviewDto mapToReviewDto(Review review) {
        return ReviewDto.builder()
                .reviewId(review.getReviewId())
                .memberId(review.getMemberId())
                .dinerId(review.getDinerId())
                .rating(review.getRating())
                .comment(review.getComment())
                .build();
    };
}