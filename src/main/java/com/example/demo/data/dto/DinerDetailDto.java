package com.example.demo.data.dto;

import com.example.demo.data.enums.DinerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DinerDetailDto {
  private Long id;
  private String dinerName;
  private String category;
  private String tel;
  private String location;
  private Double dx;
  private Double dy;
  private DinerStatus status;
  private String ownerPhone;
  private Long ownerId;
}
