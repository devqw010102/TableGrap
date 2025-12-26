package com.example.demo.data.dto;

import com.example.demo.data.model.Owner;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DinerDto {
  private String dinerName;
  private String businessNum;
  private Owner owner;
}
