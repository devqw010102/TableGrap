package com.example.demo.data.dto.notification;

/*
    User 가 예약을 했을 때
    수신 받을 OwnerId, 식당 이름, User 이름, 예약 시간
 */
public record ReservationCreateEvent(Long ownerId, String dinerName, String memberName, String reservationTime) {
}
