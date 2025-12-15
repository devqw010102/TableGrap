package com.example.demo.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class BookResponseDto {

    private String dinerName;
    private LocalDate bookingDate;
    private Integer personnel;
    private String memberName;
    private Boolean success;
}