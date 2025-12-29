package com.example.demo.data.dto.notification;

/*
    Owner가 예약 거절했을 때 사용(Owner -> user)
    수신받을 memberId, 식당 이름, 예약 시간
 */
public record ReservationRejectEvent(Long memberId, String dinerName, String reservationTime) {
}
