package com.example.demo.data.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewChartByDinerDto {
    private Long dinerId;
    private String dinerName;
    private String createTime;  // 쿼리에서 문자열로 변환하기 때문에 dto에서 string으로 받음
    private Double averageRating;
    private Long reviewCount;
}
