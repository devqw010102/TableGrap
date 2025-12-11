package com.example.demo.service.impl;

import com.example.demo.data.dto.MemberDto;
import com.example.demo.data.model.Member;
import com.example.demo.data.repository.MemberRepository;
import com.example.demo.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder; // need @Bean - config

    private MemberDto mapToMemberDto(Member member) {
        return MemberDto.builder()
                .id(member.getId())
                .userName(member.getUserName())
                .name(member.getName())
                .email(member.getEmail())
                .phone(member.getPhone())
                .build();
    }

    @Override
    public MemberDto getMemberById(Long id) {
        return memberRepository.findById(id).map(this::mapToMemberDto).orElseThrow();
    }

    @Override
    public MemberDto createMember(MemberDto memberDto) {
        Member member = Member.builder()
                .name(memberDto.getName())
                .userName(memberDto.getUserName())
                .email(memberDto.getEmail())
                .phone(memberDto.getPhone())
                .password(passwordEncoder.encode(memberDto.getPassword()))
                .build();
        memberRepository.save(member);
        return mapToMemberDto(member);
    }
    @Override
    public Optional<MemberDto> findByEmail(String email) {
        return memberRepository.findByEmail(email).
                map(this::mapToMemberDto);
    }

    @Override
    public boolean isUsernameDuplicate(String username) {
        return memberRepository.existsByUserName(username);
    }
}
