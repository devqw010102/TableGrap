package com.example.demo.data.dto;

import lombok.*;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DinerListDto {
    private Long id;
    private String dinerName;
    private String category;
}
