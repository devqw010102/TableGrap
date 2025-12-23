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
    @Pattern(regexp = "^[가-힣]{2,10}$", message = "이름은 한글만 가능합니다")
    private String name;

    @NotBlank(message ="아이디를 입력하세요")
    @Size(min = 3, max = 12, message = "아이디는 3~12자 사이여야 합니다")
    @Pattern(regexp = "^[a-zA-Z0-9]{3,12}$",
            message = "아이디는 3~12자의 영문 대소문자와 숫자만 사용 가능합니다")
    private String username;

    @NotBlank(message = "이메일을 입력하세요")
    @Pattern(regexp = "^[a-zA-Z0-9]{2,}(?:\\.[a-zA-Z0-9]+)*@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
            message = "이메일 앞자리는 2자 이상이어야 하며, 유효한 형식이어야 합니다. (예: user@example.com)")
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
