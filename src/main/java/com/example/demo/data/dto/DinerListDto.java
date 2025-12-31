package com.example.demo.data.dto;

import com.example.demo.data.enums.DinerStatus;
import lombok.*;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DinerListDto {
    private Long id;
    private String dinerName;
    private String location;
    private DinerStatus status;
    private String category;
}
