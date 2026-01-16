package com.example.demo.data.dto;

import com.example.demo.data.enums.DinerStatus;
import lombok.*;

import java.util.List;

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
    private List<String> keywords;
}
