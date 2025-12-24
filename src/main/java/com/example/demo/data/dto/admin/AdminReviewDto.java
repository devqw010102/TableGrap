package com.example.demo.data.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AdminReviewDto {
    private Long reviewId;
    private String memberUsername;
    private String dinerName;
    private int rating;
    private String comment;
    private LocalDateTime createTime;
}
