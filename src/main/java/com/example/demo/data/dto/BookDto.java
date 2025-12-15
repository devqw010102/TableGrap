package com.example.demo.data.dto;

import com.example.demo.data.model.Diner;
import com.example.demo.data.model.Member;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Builder
@Data
public class BookDto {
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDate bookingDate;
    private Integer personnel;
}
