package com.example.demo.data.dto.notification;

/*
    Owner 가 예약 취소 요청을 할 때 사용(Owner -> User)
    수신받을 MemberId, 식당 이름, 예약 시간
 */

public record ReservationCancelRequestEvent(Long memberId, String dinerName, String reservationTime) {
}
