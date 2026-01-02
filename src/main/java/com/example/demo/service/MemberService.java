package com.example.demo.service;

import com.example.demo.data.dto.MemberDto;
import com.example.demo.data.dto.MemberInfoResponseDto;
import com.example.demo.data.dto.MemberUpdateDto;
import com.example.demo.data.model.Member;

import java.util.Optional;

public interface MemberService {
    // register
    MemberDto createMember(MemberDto memberDto);

    Optional<MemberDto> findByEmail(String email);  // 이메일 확인

    boolean isUsernameDuplicate(String username);   // id 확인

    // myPage
    MemberInfoResponseDto findMyInfo(Long memberId);

    Member getMember(String username);

    // myPage edit
    MemberUpdateDto updateMember(Long memberId, MemberUpdateDto dto); // update

    boolean deleteMember(Long memberId, String checkPassword);

    // 아이디 찾기
    Optional<String> findIdByNameAndEmail(String name, String email);

    // 비밀번호 재설정 전
    boolean existsByUsernameAndEmail(String username, String email);

    // 비밀번호 업데이트
    void updatePassword(String username, String newPassword);
}
