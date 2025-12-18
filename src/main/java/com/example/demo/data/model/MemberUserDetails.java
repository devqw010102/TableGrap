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

    private final String userName;  // 아이디
    private final String password;  // 비밀번호
    private final List<SimpleGrantedAuthority> authorities;     // 권한
    private final String displayName;   // 이름
    private final Long memberId;        // 회원 번호(column)

    public MemberUserDetails(Member member, List<Authority> authorities) {
        this.userName = member.getUsername();
        this.password = member.getPassword();
        this.displayName = member.getName();
        this.memberId = member.getId();
        this.authorities = authorities.stream().map(authority -> new SimpleGrantedAuthority(authority.getAuthority())).toList();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities; // 권한 반환
    }

    @Override
    public String getPassword() {
        return password; // Member의 DB 비밀번호 그대로
    }

    @Override
    public String getUsername() {
        return userName; // 로그인에 사용할 username 또는 email
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
}
