package com.example.demo.controller;

import com.example.demo.data.userDeatils.OwnerUserDetails;
import com.example.demo.service.OwnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ownerPage/charts")
public class OwnerChartController {
    private final OwnerService ownerService;

    @GetMapping(value = "/generate/review_chart", produces = "application/json; charset=UTF-8")
    public ResponseEntity<String> generateChart(@AuthenticationPrincipal OwnerUserDetails userDetails) {
        // 이 안에서 pythonProcessExecutor.execute("owner", "review_chart", jsonData)가 호출되어야 함
        String chartJson = ownerService.generateReviewChart(userDetails.getOwner().getId());
        return ResponseEntity.ok(chartJson);
    }


    @GetMapping(value = "/generate/revisit_chart", produces = "aplication/json; charset=UTF-8")
    public ResponseEntity<String> generateRevisitChart(@AuthenticationPrincipal OwnerUserDetails userDetails) {
        String chartJson = ownerService.generateRevisitsChart(userDetails.getOwner().getId());
        return ResponseEntity.ok(chartJson);
    }


    @GetMapping(value = "/generate/revisit_chart/{dinerId}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<String> genRevisitsChartByDiner(@PathVariable Long dinerId,
                                          @AuthenticationPrincipal OwnerUserDetails userDetails) {
        String chartJson = ownerService.genRevisitsChartByDiner(dinerId, userDetails.getOwner().getId());
        return ResponseEntity.ok(chartJson);
    }

    @GetMapping(value="/generate/review_chart/{dinerId}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<String> genReviewChartByDiner(@PathVariable Long dinerId,
                                                        @AuthenticationPrincipal OwnerUserDetails userDetails) {
        String chartJson = ownerService.genReviewChartByDiner(dinerId, userDetails.getOwner().getId());
        return ResponseEntity.ok(chartJson);
    }
}
