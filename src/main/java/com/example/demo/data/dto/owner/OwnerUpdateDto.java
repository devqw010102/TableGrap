package com.example.demo.data.dto.owner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OwnerUpdateDto {
    private String email;
    private String phone;
    private String password;
}
