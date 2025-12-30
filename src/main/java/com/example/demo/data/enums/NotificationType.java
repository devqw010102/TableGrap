package com.example.demo.data.enums;

public enum NotificationType {
    // OWNER 케이스: 예약 생성, 예약 수정, 리뷰 작성 시 관리자 페이지로
    RESERVATION_CREATE("/ownerPage"),
    RESERVATION_UPDATE("/ownerPage"),
    REVIEW_WRITE("/ownerPage"),
    RESERVATION_CANCEL("/ownerPage"),

    // USER 케이스: 예약 승인, 취소, 거절 시 마이페이지로
    RESERVATION_APPROVE("/mypage"),
    RESERVATION_REJECT("/mypage"),

    // 이동이 필요 없는 일반 알림
    NONE(null);

    private final String targetUrl;

    NotificationType(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public String getTargetUrl() {
        return targetUrl;
    }
}