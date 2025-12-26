package com.example.demo.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookDto {
    private Long bookId;    // update
    private Long dinerId;   // update
    private Long memberId;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime bookingDate;
    private Integer personnel;
}
