package com.example.demo.controller;

import com.example.demo.data.dto.ReviewDto;
import com.example.demo.data.model.MemberUserDetails;
import com.example.demo.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/mypage")
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping("/review/create")
    @ResponseBody // AJAX 응답을 위해 추가
    public ResponseEntity<?> createReview(@RequestBody ReviewDto reviewDto, @AuthenticationPrincipal MemberUserDetails user) {
        System.out.println("식당ID: " + reviewDto.getDinerId());
        reviewService.createReview(reviewDto, user.getMemberId());
        return ResponseEntity.ok().build();
    }
}