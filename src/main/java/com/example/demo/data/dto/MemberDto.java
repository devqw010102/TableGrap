package com.example.demo.data.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Size(max = 10, message = "이름은 10자 이내여야 합니다")
    private String name;

    @NotBlank(message ="아아디를 입력하세요")
    @Size(min = 4, max = 20, message = "아이디는 4~20자 사이여야 합니다")
    @Pattern(regexp = "^[a-z0-9]*$", message = "아이디는 영문 소문자와 숫자만 사용 가능합니다")
    private String userName;

    @NotBlank(message ="이메일을 입력하세요")
    @Email(message = "유효한 이메일 형식이 아닙니다")
    private String email;

    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message="전화번호 형식이 올바르지 않습니다.")
    private String phone;

    @NotBlank(message = "비밀번호를 입력하세요")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
    private String password;

    @NotBlank(message = "비밀번호 확인해주세요")
    private String passwordConfirm;
}
