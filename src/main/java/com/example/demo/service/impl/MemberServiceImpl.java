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
    private final PasswordEncoder passwordEncoder; //  @Bean 필요- config

    private MemberDto mapToMemberDto(Member member) {
        return MemberDto.builder()
                .id(member.getId())
                .userName(member.getUserName()) // 실제 id
                .name(member.getName())         // 이름
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
                .name(memberDto.getName())          // 이름
                .userName(memberDto.getUserName())  // 실제 id
                .email(memberDto.getEmail())
                .phone(memberDto.getPhone())
                .password(passwordEncoder.encode(memberDto.getPassword()))
                .build();
        memberRepository.save(member);
        return mapToMemberDto(member);
    }
    @Override
    public Optional<MemberDto> findByEmail(String email) {  // 이메일 확인
        return memberRepository.findByEmail(email).
                map(this::mapToMemberDto);
    }

    @Override
    public boolean isUsernameDuplicate(String username) {   // id 확인
        return memberRepository.existsByUserName(username);
    }
}
