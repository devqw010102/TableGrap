package com.example.demo.data.dto;

import com.example.demo.data.model.OwnerRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class OwnerRequestDto {
    private Long id;
    private String memberUsername;
    private String memberName;
    private String dinerName;
    private String location;
    private String status;
    private LocalDateTime createdAt;

    public static OwnerRequestDto from(OwnerRequest request) {
        return new OwnerRequestDto(
                request.getId(),
                request.getMember().getUsername(),
                request.getMember().getName(),
                request.getDiner().getDinerName(),
                request.getDiner().getLocation(),
                request.getStatus().name(),
                request.getCreateAt()
        );
    }
}
