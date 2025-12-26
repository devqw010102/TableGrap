package com.example.demo.data.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDto {
    private Long reviewId;
    private Long memberId;
    private Long dinerId;
    private Long bookId;
    private String dinerName;
    private int rating;
    private String comment;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    private LocalDateTime createTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    private LocalDateTime updateTime;
}