package com.example.demo.data.dto.notification;
/*
    회원 정보 수정시 사용
    수신받을 memberId, 회원의 이름
 */
public record MemberUpdateEvent(Long memberId, String name) {
}
