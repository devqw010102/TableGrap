package com.example.demo.controller;

import com.example.demo.data.dto.ReviewChartDto;
import com.example.demo.data.userDeatils.OwnerUserDetails;
import com.example.demo.service.OwnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ownerPage/charts")
public class OwnerChartController {
    private final OwnerService ownerService;

    @GetMapping("/review_chart")
    public List<ReviewChartDto> getAvgReviewCharts(@AuthenticationPrincipal OwnerUserDetails userDetails) {
       return ownerService.getAvgRate(userDetails.getOwner().getId());
    }

    @GetMapping("/generate")
    public String generateChart(@AuthenticationPrincipal OwnerUserDetails userDetails) {
        return ownerService.generateReviewChart(userDetails.getOwner().getId());
    }
}
