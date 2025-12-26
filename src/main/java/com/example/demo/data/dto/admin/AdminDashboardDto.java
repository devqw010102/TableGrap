package com.example.demo.data.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class AdminDashboardDto {
    private Long dinerCount;
    private Long todayBookingCount;
    private Long memberCount;       // 총 회원 수
    
    private Long ownerCount;        // 사장 수
    private Long userCount;         // 일반유저 수

    private List<AdminReviewDto> todayReviews;
}
