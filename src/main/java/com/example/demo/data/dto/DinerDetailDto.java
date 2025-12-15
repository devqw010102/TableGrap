package com.example.demo.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DinerDetailDto {
  private Long id;
  private String dinerName;
  private String tel;
  private String location;
  private Double dx;
  private Double dy;

  //운영시간은 어떻게?
  //후기는 schema를 어떻게 할 것인지? One to Many?
}
