package com.example.demo.data.userDeatils;

import org.springframework.security.core.userdetails.UserDetails;

public interface IdentifiableUserDetails extends UserDetails {
    Long getUserId();       // notification 사용을 위해 구현, thymeleaf 사용
}
