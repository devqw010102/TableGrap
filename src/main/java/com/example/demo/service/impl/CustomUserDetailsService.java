package com.example.demo.service.impl;

import com.example.demo.data.model.Authority;
import com.example.demo.data.model.Member;
import com.example.demo.data.model.MemberUserDetails;
import com.example.demo.data.repository.AuthorityRepository;
import com.example.demo.data.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.userdetails.User;


@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final AuthorityRepository authorityRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        List<SimpleGrantedAuthority> authorities = authorityRepository.findByMember(member).stream().map(a -> new SimpleGrantedAuthority(a.getAuthority())).toList();

        return new User(member.getUsername(), member.getPassword(), authorities);
    }
}
