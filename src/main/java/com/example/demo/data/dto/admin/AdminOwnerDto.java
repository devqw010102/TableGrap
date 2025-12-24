package com.example.demo.data.dto.admin;

import com.example.demo.data.model.Diner;
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

    public static AdminOwnerDto from(Diner diner) {
        return new AdminOwnerDto(
          diner.getOwner().getId(),
          diner.getOwner().getName(),
          diner.getOwner().getEmail(),
          diner.getOwner().getPhone(),
          diner.getId(),
          diner.getDinerName(),
          diner.getCategory(),
          diner.getStatus().toString()
        );
    }
}
