package com.example.demo.data.dto.admin;

import com.example.demo.data.model.Diner;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminDinerDto {
    private Long id;
    private String dinerName;
    private String category;
    private String tel;
    private String location;
    private String ownerName;

    public static AdminDinerDto from(Diner diner) {
        return new AdminDinerDto(
                diner.getId(),
                diner.getDinerName(),
                diner.getCategory(),
                diner.getTel(),
                diner.getLocation(),
                diner.getOwner() != null ? diner.getOwner().getName() : "미지정"
        );
    }
}
