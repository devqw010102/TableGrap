package com.example.demo.controller;

import com.example.demo.data.dto.ReviewDto;
import com.example.demo.data.model.MemberUserDetails;
import com.example.demo.data.model.Review;
import com.example.demo.data.repository.ReviewRepository;
import com.example.demo.service.ReviewService;
import com.example.demo.service.ReviewService_test;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/mypage")
public class ReviewController {
  private final ReviewService reviewService;

  @PostMapping("/review")
  @ResponseBody // AJAX 응답을 위해 추가
  public ResponseEntity<ReviewDto> createReview(@RequestBody ReviewDto reviewDto, @AuthenticationPrincipal MemberUserDetails user) {
    ReviewDto review = reviewService.createReview(
            reviewDto.getDinerId(),
            user.getMemberId(),
            reviewDto
    );
    return ResponseEntity.ok(review);
  }
}
