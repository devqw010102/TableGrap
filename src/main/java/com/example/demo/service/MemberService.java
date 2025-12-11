package com.example.demo.service;

import com.example.demo.data.dto.MemberDto;
import com.example.demo.data.model.Member;

import java.util.Optional;

public interface MemberService {
    MemberDto getMemberById(Long id);

        MemberDto createMember(MemberDto memberDto);

        Optional<MemberDto> findByEmail(String email);

        boolean isUsernameDuplicate(String username);
    }