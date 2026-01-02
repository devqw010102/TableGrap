package com.example.demo.data.dto;

import com.example.demo.data.model.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MemberInfoResponseDto {
    private Long id;
    private String username;
    private String name;
    private String email;
    private String phone;

    public static MemberInfoResponseDto from(Member m) {
        return new MemberInfoResponseDto(
                m.getId(),
                m.getUsername(),
                m.getName(),
                m.getEmail(),
                m.getPhone()
        );
    }
}
