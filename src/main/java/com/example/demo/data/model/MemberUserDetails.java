package com.example.demo.data.model;

import lombok.Data;
import lombok.Getter;
// import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class MemberUserDetails implements UserDetails {

    private final Member member;
    private final List<Authority> authorities;

    public MemberUserDetails(Member member, List<Authority> authorities) {
        this.member = member;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities.stream()
                .map(a -> new SimpleGrantedAuthority(a.getAuthority()))
                .toList();
    }

    @Override
    public String getPassword() {
        return member.getPassword(); // Member의 DB 비밀번호 그대로
    }

    @Override
    public String getUsername() {
        return member.getUsername(); // 로그인에 사용할 username 또는 email
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 만료 기능 안 쓰면 true
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 잠금 기능 안 쓰면 true
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 비밀번호 만료 기능 안 쓰면 true
    }

    @Override
    public boolean isEnabled() {
        return true; // 활성화 상태
    }

    public Long getMemberId() {
        return member.getId(); // 리뷰가져오기 할 때 필요
    }
}
