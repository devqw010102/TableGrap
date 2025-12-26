package com.example.demo.data.dto;

import com.example.demo.data.enums.DinerStatus;
import lombok.*;

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

  //운영시간은 어떻게?
  //후기는 schema를 어떻게 할 것인지? One to Many?
}
