package com.example.demo.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MemberUpdateDto {

    private String email;   // 이메일
    private String phone;   // 핸드폰
    private String password;    // 비밀번호
    private String passwordConfirm;  // 비밀번호 수정
}

