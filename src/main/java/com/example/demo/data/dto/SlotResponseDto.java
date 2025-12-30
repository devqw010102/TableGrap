package com.example.demo.data.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SlotResponseDto {
    private String time;
    private int currentCapacity;
    private boolean isAvailable;
}
