package com.example.demo.data.dto.notification;

import java.time.LocalDateTime;
/*
    Owner가 예약 승인했을 때 사용(Owner -> user) 
    수신받을 memberId, 식당 이름, 예약 시간

 */
public record ReservationApproveEvent(Long memberId, String dinerName, LocalDateTime reservationTime) {
}
