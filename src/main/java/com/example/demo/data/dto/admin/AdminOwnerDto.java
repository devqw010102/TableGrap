package com.example.demo.data.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AdminOwnerDto {
    private Long memberId;
    private String ownerName;
    private String email;
    private String phone;
    private Long dinerId;
    private String dinerName;
    private String category;
    private String status;
}
