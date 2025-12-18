package com.example.demo.data.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class BookOwnerResponseDto {
    private Long bookId;
    private String dinerName;
    private LocalDateTime bookingDate;
    private Integer personnel;
    private String memberName;
}
