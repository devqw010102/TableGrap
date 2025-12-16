package com.example.demo.data.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class BookResponseDto {

    private Long bookId;    //예약 수정용
    private Long dinerId;   //예약 수정용

    private String dinerName;

    // @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    private LocalDateTime bookingDate;
    private Integer personnel;
    private String memberName;
    private Boolean success;
}