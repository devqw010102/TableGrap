package com.example.demo.service;

import com.example.demo.data.dto.ReviewDto;
import com.example.demo.data.dto.admin.AdminReviewDto;
import com.example.demo.data.dto.owner.OwnerReviewDto;
import com.example.demo.data.model.Member;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ReviewService {
    List<ReviewDto> getTop5Reviews(Long dinerId);
    List<ReviewDto> getAllMyReview(Long memberId);
    //리뷰수정을 위해 리뷰를 가져옴
    Optional<ReviewDto> getReview(Long reviewId);
    //리뷰 수정
    void updateReview(Long reviewId, ReviewDto reviewDto);
    @Transactional
    void createReview(ReviewDto reviewDto, Long memberId);
    @Transactional
    void deleteReview(Long reviewId);
    List<AdminReviewDto> getAll();
    
    // OwnerPage 리뷰
    Page<OwnerReviewDto> getOwnerReviews(Member owner, Long dinerId, int page, int size);
}
