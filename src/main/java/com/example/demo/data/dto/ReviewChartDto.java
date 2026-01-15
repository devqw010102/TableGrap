package com.example.demo.data.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewChartDto {
    Long dinerId;
    Double averageRating;
    Long reviewCount;
}
