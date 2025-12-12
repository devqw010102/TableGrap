package com.example.demo.service.impl;

import com.example.demo.data.dto.MemberDto;
import com.example.demo.data.model.Authority;
import com.example.demo.data.model.Member;
import com.example.demo.data.repository.AuthorityRepository;
import com.example.demo.data.repository.MemberRepository;
import com.example.demo.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder; //  @Bean 필요- config
    private final AuthorityRepository authorityRepository;

    private MemberDto mapToMemberDto(Member member) {
        return MemberDto.builder()
                .id(member.getId())
                .username(member.getUsername()) // 실제 id
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
        String phoneNumber = memberDto.getPhone();
        if (phoneNumber != null && phoneNumber.isEmpty()) {
            phoneNumber = null;
        }

        Member member = Member.builder()
                .name(memberDto.getName())          // 이름
                .username(memberDto.getUsername())  // 실제 id
                .email(memberDto.getEmail())
                .phone(phoneNumber)
                .password(passwordEncoder.encode(memberDto.getPassword()))
                .build();
        memberRepository.save(member);

        Authority authority = Authority.builder()
                .authority("ROLE_USER")
                .member(member)
                .build();

        authorityRepository.save(authority);

        return mapToMemberDto(member);
    }
    @Override
    public Optional<MemberDto> findByEmail(String email) {  // 이메일 확인
        return memberRepository.findByEmail(email).
                map(this::mapToMemberDto);
    }

    @Override
    public boolean isUsernameDuplicate(String username) {   // id 확인
        return memberRepository.existsByUsername(username);
    }

    @Override
    public List<Member> getList() {
        return memberRepository.findAll();
    }
}
