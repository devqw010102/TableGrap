package com.example.demo.data.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MemberUpdateDto {

    @Email(message = "유효한 이메일 형식이 아닙니다")
    private String email;   // 이메일

    @Pattern(regexp = "^$|^\\d{2,3}-\\d{3,4}-\\d{4}$", message="전화번호 형식이 올바르지 않습니다.")
    private String phone;   // 핸드폰

    @NotBlank(message = "비밀번호를 입력하세요")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
    private String password;    // 비밀번호

    @NotBlank(message = "비밀번호 확인해주세요")
    private String passwordConfirm;  // 비밀번호 수정 doubl check
}

