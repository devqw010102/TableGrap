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
    @Pattern(regexp = "^[a-zA-Z가-힣]+$", message = "이름은 한글 또는 영문만 가능합니다")
    private String name;

    @NotBlank(message ="아아디를 입력하세요")
    @Size(min = 4, max = 20, message = "아이디는 4~20자 사이여야 합니다")
    @Pattern(regexp = "^[a-z0-9]*$", message = "아이디는 영문 소문자와 숫자만 사용 가능합니다")
    private String username;

    @NotBlank(message = "이메일을 입력하세요")
    @Pattern(regexp = "^[a-zA-Z0-9]([a-zA-Z0-9._-]{0,62}[a-zA-Z0-9])?@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
            message = "유효한 이메일 형식이 아닙니다 (예: user@example.com)")
    private String email;

    @Pattern( regexp = "^$|^01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}$", message = "(-) 제외 번호만 입력해주세요.")
    private String phone;

    @NotBlank(message = "비밀번호를 입력하세요")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()+|=])[A-Za-z\\d~!@#$%^&*()+|=]{8,16}$",
            message = "비밀번호는 영문, 숫자, 특수문자를 포함하여 8~16자여야 합니다")
    private String password;

    @NotBlank(message = "비밀번호 확인해주세요")
    private String passwordConfirm;
}
