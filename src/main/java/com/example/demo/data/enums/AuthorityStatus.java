package com.example.demo.data.enums;

import lombok.Getter;

@Getter
public enum AuthorityStatus {
    ROLE_ADMIN("ROLE_ADMIN", "관리자"),
    ROLE_USER("ROLE_USER", "일반 사용자"),
    ROLE_OWNER("ROLE_OWNER", "사장 사용자"),
    ROLE_DELETED("ROLE_DELETED", "삭제된 계정");

    private final String code;
    private final String description;

    AuthorityStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

}