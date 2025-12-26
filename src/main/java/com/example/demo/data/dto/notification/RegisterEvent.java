package com.example.demo.data.dto.notification;

/*
    회원가입 성공했을 때 사용(System -> user or owner)
    수신받을 memberId
 */
public record RegisterEvent(Long memberId) {
}
