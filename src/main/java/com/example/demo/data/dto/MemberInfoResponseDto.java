package com.example.demo.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemberInfoResponseDto {
    private String username;
    private String name;
    private String email;
    private String phone;
}
