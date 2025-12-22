package com.example.demo.data.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AdminBookDto {
    Long id;
    LocalDateTime createDate;
    String dinerName;
    LocalDateTime bookingDate;
    Integer personnel;
    String memberName;
    Boolean success;
}
