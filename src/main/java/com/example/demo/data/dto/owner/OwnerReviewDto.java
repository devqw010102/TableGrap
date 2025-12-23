package com.example.demo.data.dto.owner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class OwnerReviewDto {
    private Long reviewId;
    private String memberUsername;
    private String dinerName;
    private int rating;
    private String comment;
    private LocalDateTime createTime;
}
