package com.example.demo.controller;


import com.example.demo.data.dto.ReviewDto;
import com.example.demo.data.model.MemberUserDetails;
import com.example.demo.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/review")
public class ReviewRestController {
  private final ReviewService reviewService;


  @GetMapping("/list")
  public  List<ReviewDto> getTop5Reviews(Long dinerId, @AuthenticationPrincipal MemberUserDetails user, Model model) {
    List<ReviewDto> reviews = reviewService.getTop5Reviews(dinerId, user.getMemberId());
    model.addAttribute("reviews", reviews);
    return reviews;
  }
}
