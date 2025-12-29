package com.example.demo.data.dto.notification;

/*
    User 가 예약 취소했을 때 사용(User -> Owner)
    수신 받을 OwnerId, 식당 이름, User 이름, 예약 시간
 */
public record ReservationCancelEvent(Long ownerId, String dinerName, String memberName, String reservationTime) {
}
