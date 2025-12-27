package com.example.demo.data.dto.owner;

import com.example.demo.data.enums.DinerStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OwnerDinerDto {
    private Long id;
    private String dinerName;
    private DinerStatus status;
}
