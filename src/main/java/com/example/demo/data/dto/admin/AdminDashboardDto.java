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
    private Long memberCount;

    private Long ownerCount;
    private Long userCount;

    private List<AdminReviewDto> todayReviews;
}
