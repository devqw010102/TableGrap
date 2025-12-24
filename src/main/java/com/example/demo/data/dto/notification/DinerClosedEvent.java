package com.example.demo.data.dto.notification;

/*
    식당 폐업 처리 시 사용(Owner -> 예약이 있는 User 들)
    식당 ID, 식당 이름
 */
public record DinerClosedEvent(Long dinerId, String dinerName) {
}
