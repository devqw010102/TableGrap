package com.example.demo.controller;


import com.example.demo.data.dto.ReviewDto;
import com.example.demo.data.userDeatils.MemberUserDetails;
import com.example.demo.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/review")
public class ReviewRestController {
    private final ReviewService reviewService;

    //reservation.html에서 식당 후기 출력
    @GetMapping("/reservation/list")
    public List<ReviewDto> getTop5Reviews(@RequestParam(value = "dinerId") Long dinerId) {
        return reviewService.getTop5Reviews(dinerId);
        //model.addAttribute("reviews", reviewList); 비동기에는 이거 안쓴다...
    }

    @GetMapping("/list")
    public List<ReviewDto> getAllReviews(@AuthenticationPrincipal MemberUserDetails user) {
        return reviewService.getAllMyReview(user.getMember().getId());
    }

    @PostMapping("/create")
    public ResponseEntity<?> createReview(@RequestBody ReviewDto reviewDto, @AuthenticationPrincipal MemberUserDetails user) {
        System.out.println("식당ID: " + reviewDto.getDinerId());
        reviewService.createReview(reviewDto, user.getMember().getId());
        return ResponseEntity.ok().build();
    }

    //리뷰 수정 모달에서 리뷰 내용 출력을 위함
    @GetMapping("/{reviewId:[0-9]+}")
    public Optional<ReviewDto> getMyReview(@PathVariable Long reviewId) {
        return reviewService.getReview(reviewId);
    }

    @PatchMapping("/update/{reviewId:[0-9]+}")
    public void updateMyReview(@PathVariable Long reviewId, @RequestBody ReviewDto reviewDto) {
        reviewService.updateReview(reviewId, reviewDto);
    }

    @DeleteMapping ("/delete/{reviewId:[0-9]+}")
    public void deleteMyReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
    }

}