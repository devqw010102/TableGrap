package com.example.demo.controller;


import com.example.demo.data.dto.ReviewDto;
import com.example.demo.data.model.MemberUserDetails;
import com.example.demo.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/review")
public class ReviewRestController {
    private final ReviewService reviewService;

    @GetMapping("/list")
    //마이페이지에서 내가 쓴 리뷰 전체를 보기 위해서 required=false사용
    public List<ReviewDto> getReviews(@RequestParam(value = "dinerId", required = false) Long dinerId,
                                      @AuthenticationPrincipal MemberUserDetails user) {
        if(user == null){
            return Collections.emptyList(); //로그인하지 않았으면 빈 목록
        }
        //식당 페이지
        if(dinerId != null){
            return reviewService.getTop5Reviews(dinerId);
        } else {
            //마이 페이지
            return reviewService.getAllMyReview(user.getMember().getId());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createReview(@RequestBody ReviewDto reviewDto, @AuthenticationPrincipal MemberUserDetails user) {
        System.out.println("식당ID: " + reviewDto.getDinerId());
        reviewService.createReview(reviewDto, user.getMember().getId());
        return ResponseEntity.ok().build();
    }
    //리뷰 수정 모달에서 리뷰 내용 출력을 위함
    @GetMapping("/{reviewId}")
    public Optional<ReviewDto> getMyReview(@PathVariable Long reviewId) {
        return reviewService.getReview(reviewId);
    }

    @PatchMapping("/update/{reviewId}")
    public void updateMyReview(@PathVariable Long reviewId, @RequestBody ReviewDto reviewDto) {
        reviewService.updateReview(reviewId, reviewDto);
    }

    @DeleteMapping ("/delete/{reviewId}")
    public void deleteMyReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
    }

}