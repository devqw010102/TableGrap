package com.example.demo.data.enums;

public enum DinerStatus {
    PUBLIC,     // 예약 가능, 사장 O
    CLOSED,     // 예약 불가능, 사장 X
    DELETED     // 예약 불가능, 사장 X, List 에서도 Invisible
}
