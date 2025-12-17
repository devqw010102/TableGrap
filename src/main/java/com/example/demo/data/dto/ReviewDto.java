package com.example.demo.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDto {
  private Long reviewId;
  private Long memberId;
  private Long dinerId;
  private int rating;
  private String comment;
}
