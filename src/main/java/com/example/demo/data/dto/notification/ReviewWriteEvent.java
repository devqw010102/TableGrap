package com.example.demo.data.dto.notification;
/*
    User가 리뷰를 작성했을 때 사용(User -> owner)
    수신받을 OwnerId, 식당 이름
 */
public record ReviewWriteEvent(Long ownerId, String dinerName) {
}
