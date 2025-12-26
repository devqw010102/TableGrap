package com.example.demo.service;

import com.example.demo.data.dto.MemberDto;
import com.example.demo.data.dto.MemberInfoResponseDto;
import com.example.demo.data.dto.MemberUpdateDto;
import com.example.demo.data.model.Member;

import java.util.Optional;

public interface MemberService {
    // register
    MemberDto getMemberById(Long id);
    MemberDto createMember(MemberDto memberDto);
    Optional<MemberDto> findByEmail(String email);  // 이메일 확인
    boolean isUsernameDuplicate(String username);   // id 확인




    // myPage
    MemberInfoResponseDto findMyInfo(Long memberId);
    Member getMember(String username);

    // myPage edit
    MemberUpdateDto updateMember(Long memberId, MemberUpdateDto dto); // update
    boolean deleteMember(Long memberId, String checkPassword);
}
