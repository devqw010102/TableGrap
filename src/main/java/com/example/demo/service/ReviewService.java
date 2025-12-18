package com.example.demo.service;

import com.example.demo.data.dto.ReviewDto;

import java.util.List;

public interface ReviewService {
  List<ReviewDto> getTop5Reviews(Long dinerId, Long memberId);
  List<ReviewDto> getAllMyReview(Long memberId);
  public ReviewDto createReview(Long dinerId, Long memberId, ReviewDto reviewDto);
}
