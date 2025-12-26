package com.example.demo.data.dto.notification;
/*
    Owner 가 회원 수정시 사용
    수신받을 memberId, 회원의 이름
 */


public record OwnerUpdateEvent(Long memberId, String name) {
}
