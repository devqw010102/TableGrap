package com.example.demo.data.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class MemberDto {
    private Long id;

    @NotBlank(message = "이름을 입력하세요")
    private String name;

    @NotBlank(message ="아아디를 입력하세요")
    private String userName;

    @NotBlank(message ="이메일을 입력하세요")
    private String email;

    private String phone;

    @NotBlank(message = "비밀번호를 입력하세요")
    private String password;

    @NotBlank(message = "비밀번호 확인해주세요")
    private String passwordConfirm;
}
