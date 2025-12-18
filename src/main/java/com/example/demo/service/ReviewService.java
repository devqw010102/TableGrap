package com.example.demo.service;

import com.example.demo.data.dto.ReviewDto;
import jakarta.transaction.Transactional;

import java.util.List;

public interface ReviewService {
    List<ReviewDto> getTop5Reviews(Long dinerId, Long memberId);
    List<ReviewDto> getAllMyReview(Long memberId);
    @Transactional
    public ReviewDto createReview(ReviewDto reviewDto, Long memberId);
}
