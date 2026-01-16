package com.example.demo.controller;

import com.example.demo.data.dto.ReviewChartDto;
import com.example.demo.data.dto.RevisitDto;
import com.example.demo.data.userDeatils.OwnerUserDetails;
import com.example.demo.service.OwnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @GetMapping(value = "/generate", produces = "application/json; charset=UTF-8")
    public ResponseEntity<String> generateChart(@AuthenticationPrincipal OwnerUserDetails userDetails) {
        // 이 안에서 pythonProcessExecutor.execute("owner", "review_chart", jsonData)가 호출되어야 함
        String chartJson = ownerService.generateReviewChart(userDetails.getOwner().getId());
        return ResponseEntity.ok(chartJson);
    }

    @GetMapping("/revisit_chart")
    public List<RevisitDto> revisitChart(@AuthenticationPrincipal OwnerUserDetails userDetails) {
        return ownerService.getRevisits(userDetails.getOwner().getId());
    }

    @GetMapping(value = "/generate/revisit_chart", produces = "aplication/json; charset=UTF-8")
    public ResponseEntity<String> generateRevisitChart(@AuthenticationPrincipal OwnerUserDetails userDetails) {
        String chartJson = ownerService.generateRevisitsChart(userDetails.getOwner().getId());
        return ResponseEntity.ok(chartJson);
    }

    @GetMapping("/revisit_chart/{dinerId}")
    public List<RevisitDto> revisitChartByDimer(@PathVariable Long dinerId,
                                                @AuthenticationPrincipal OwnerUserDetails userDetails) {
        return ownerService.getBookByDinerId(dinerId, userDetails.getOwner().getId());
    }

    @GetMapping(value = "/generate/revisit_chart/{dinerId}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<String> genRevisitsChartByDiner(@PathVariable Long dinerId,
                                          @AuthenticationPrincipal OwnerUserDetails userDetails) {
        String chartJson = ownerService.genRevisitsChartByDiner(dinerId, userDetails.getOwner().getId());
        return ResponseEntity.ok(chartJson);
    }
}
