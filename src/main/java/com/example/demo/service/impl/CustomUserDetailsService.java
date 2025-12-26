package com.example.demo.service.impl;

import com.example.demo.data.model.*;
import com.example.demo.data.repository.AuthorityRepository;
import com.example.demo.data.repository.MemberRepository;
import com.example.demo.data.repository.OwnerRepository;
import com.example.demo.data.userDeatils.MemberUserDetails;
import com.example.demo.data.userDeatils.OwnerUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final AuthorityRepository authorityRepository;
    private final OwnerRepository ownerRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        Member member = memberRepository.findByUsername(username).orElse(null);
        if(member != null) {
            List<Authority> authorities = authorityRepository.findByMember(member);
            return new MemberUserDetails(member, authorities);
        }

        Owner owner = ownerRepository.findByUsername(username).orElse(null);
        if(owner != null) {
            List<Authority> authorities = authorityRepository.findByOwner(owner);
            return new OwnerUserDetails(owner, authorities);
        }
        throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
    }
}
