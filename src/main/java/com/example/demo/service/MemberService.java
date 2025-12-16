package com.example.demo.service;

import com.example.demo.data.dto.MemberDto;
import com.example.demo.data.dto.MemberInfoResponseDto;
import com.example.demo.data.model.Member;

import java.util.List;
import java.util.Optional;

public interface MemberService {
    MemberDto getMemberById(Long id);

    MemberDto createMember(MemberDto memberDto);

    Optional<MemberDto> findByEmail(String email);  // 이메일 확인

    boolean isUsernameDuplicate(String username);   // id 확인

    List<MemberInfoResponseDto> getList();

    MemberInfoResponseDto findMyInfo(Long memberId);

    Member getMember(String username);
}
