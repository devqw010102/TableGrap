package com.example.demo.data.dto;

import com.example.demo.data.model.Diner;
import com.example.demo.data.model.Member;
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
    private Long diner;
    private String memberId;


    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime bookingDate;
    private Integer personnel;
}
