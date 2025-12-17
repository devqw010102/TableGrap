package com.example.demo.service.impl;

import com.example.demo.data.dto.ReviewDto;
import com.example.demo.data.model.Review;
import com.example.demo.data.repository.ReviewRepository;
import com.example.demo.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
  private final ReviewRepository reviewRepository;

  @Override
  public List<ReviewDto> getTop5Reviews(Long dinerId, Long memberId) {
    return reviewRepository.findTop5ByDinerId(dinerId, memberId).stream().map(this::mapToReviewDto).toList();
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
