package com.example.demo.service.impl;

import com.example.demo.data.dto.MemberDto;
import com.example.demo.data.dto.MemberInfoResponseDto;
import com.example.demo.data.dto.MemberUpdateDto;
import com.example.demo.data.dto.notification.MemberUpdateEvent;
import com.example.demo.data.dto.notification.RegisterEvent;
import com.example.demo.data.enums.AccountStatus;
import com.example.demo.data.model.Authority;
import com.example.demo.data.model.Member;
import com.example.demo.data.model.Owner;
import com.example.demo.data.repository.AuthorityRepository;
import com.example.demo.data.repository.MemberRepository;
import com.example.demo.data.repository.OwnerRepository;
import com.example.demo.service.MemberService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

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
    @Transactional
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

        eventPublisher.publishEvent(new RegisterEvent(
                member.getId(),
                member.getName(),
                authority.getAuthority()
        ));

        return mapToMemberDto(member);
    }

    // validation check
    @Override
    public Optional<MemberDto> findByEmail(String email) {  // 이메일 확인
        Optional<MemberDto> member = memberRepository.findByEmail(email).map(this::mapToMemberDto);
        if (member.isPresent()) return member;

        return ownerRepository.findByEmailAndStatus(email, AccountStatus.ACTIVE)
                .map(o -> MemberDto.builder().email(o.getEmail()).build());
    }

    @Override
    public boolean isUsernameDuplicate(String username) {   // id 확인
        return memberRepository.existsByUsername(username) || ownerRepository.existsByUsername(username, AccountStatus.DELETED);
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

        eventPublisher.publishEvent(new MemberUpdateEvent(
                member.getId(),
                member.getName()
        ));

        return MemberUpdateDto.builder()
                .email(member.getEmail())
                .phone(member.getPhone())
                .build();
    }

    // 아이디 찾기, 비번 재설정
    // 아이디 찾기
    @Override
    public Optional<String> findIdByNameAndEmail(String name, String email) {
        // Member 테이블에서 검색
        Optional<String> memberUsername = memberRepository.findByNameAndEmail(name, email)
                .map(Member::getUsername);
        if (memberUsername.isPresent()) return memberUsername;

        //  Member에 없으면 Owner 테이블에서 검색
        return ownerRepository.findByNameAndEmailAndStatus(name, email, AccountStatus.ACTIVE)
                .map(Owner::getUsername);
    }

    // 비번 재설정
    @Override
    public boolean existsByUsernameAndEmail(String username, String email) {
        return memberRepository.existsByUsernameAndEmail(username, email) ||
                ownerRepository.existsByUsernameAndEmailAndStatus(username, email, AccountStatus.ACTIVE);
    }

    @Transactional
    @Override
    public void updatePassword(String username, String newPassword) {
        // Member에서 찾기
        Optional<Member> member = memberRepository.findByUsername(username);
        if (member.isPresent()) {
            member.get().setPassword(passwordEncoder.encode(newPassword));
            return;
        }

        // Member에 없으면 Owner에서 찾기
        Owner owner = ownerRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        owner.setPassword(passwordEncoder.encode(newPassword));
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
