package com.example.demo.service.impl;

import com.example.demo.data.dto.MemberDto;
import com.example.demo.data.dto.MemberInfoResponseDto;
import com.example.demo.data.dto.MemberUpdateDto;
import com.example.demo.data.model.Authority;
import com.example.demo.data.model.Member;
import com.example.demo.data.repository.AuthorityRepository;
import com.example.demo.data.repository.MemberRepository;
import com.example.demo.data.repository.OwnerRepository;
import com.example.demo.service.MemberService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final OwnerRepository ownerRepository;
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

    // Create
    @Override
    public MemberDto createMember(MemberDto memberDto) {
        String phoneNumber = memberDto.getPhone();
        if (phoneNumber != null && phoneNumber.isEmpty()) {
            phoneNumber = null;

            String email = memberDto.getEmail();
            if (email == null || email.trim().isEmpty() || email.equals("@")) {
               email = null;
            }
        }

        Member member = Member.builder()
                .id(memberDto.getId())
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

    // validation check
    @Override
    public Optional<MemberDto> findByEmail(String email) {  // 이메일 확인
        Optional<MemberDto> member = memberRepository.findByEmail(email).map(this::mapToMemberDto);
        if (member.isPresent()) return member;

        return ownerRepository.findByEmail(email)
                .map(o -> MemberDto.builder().email(o.getEmail()).build());
    }

    @Override
    public boolean isUsernameDuplicate(String username) {   // id 확인
        return memberRepository.existsByUsername(username) || ownerRepository.existsByUsername(username);
    }



    @Override
    public MemberInfoResponseDto findMyInfo(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));


        return new MemberInfoResponseDto(
                member.getId(),
                member.getUsername(),
                member.getName(),
                member.getEmail(),
                member.getPhone()
        );
    }

// edit member
    @Override
    public Member getMember(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("해당 아이디의 회원이 없습니다: " + username));
    }

    // update member
    @Override
    @Transactional
    public MemberUpdateDto updateMember(Long memberId, MemberUpdateDto dto) {
        // id(순번)을 가져와서 dto에 필요한 정보만 수정 (이메일, 전화, 비밀번호(확인도)
        Member member = memberRepository.findById(memberId).
                orElseThrow(() -> new IllegalArgumentException("해당 회원이 없습니다, 다시 확인 해주세요."));


        String email = dto.getEmail();
        if (email == null || email.trim().isEmpty() || email.startsWith("@") || email.endsWith("@") || email.equals("@")) {
            member.setEmail(null);
        } else {
            member.setEmail(email);
        }

        member.setPhone(dto.getPhone());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            member.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        return MemberUpdateDto.builder()
                .email(member.getEmail())
                .phone(member.getPhone())
                .build();
    }

    // Delete member
    @Override
    @Transactional
    public boolean deleteMember(Long memberId, String checkPassword) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 없습니다."));

        // 비밀번호 검증
        if (!passwordEncoder.matches(checkPassword, member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        memberRepository.delete(member);
        return true;
    }
}
