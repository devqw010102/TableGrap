package com.example.demo.data.dto.notification;

import java.time.LocalDateTime;
/*
    User 가 예약을 수정했을 때
    수전받을 OwnerId, 식당 이름, 예약 시간
 */
public record ReservationUpdateEvent(Long memberId, String dinerName, LocalDateTime reservationTime) {
}
