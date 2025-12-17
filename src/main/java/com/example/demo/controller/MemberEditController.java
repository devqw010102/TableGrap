package com.example.demo.controller;

import com.example.demo.data.dto.MemberDto;
import com.example.demo.data.dto.MemberUpdateDto;
import com.example.demo.data.model.MemberUserDetails;
import com.example.demo.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/myPage")
@RequiredArgsConstructor
public class MemberEditController {

    private final MemberService memberService;

    // edit
    @PostMapping("/update")
    public void updateMember(
            @AuthenticationPrincipal MemberUserDetails userDetails,
            @RequestBody MemberUpdateDto memberUpdateDto) {
        memberService.updateMember(userDetails.getMemberId(), memberUpdateDto);

    }

    // delete
    @PostMapping("/delete")
    public void deleteMember(
            @AuthenticationPrincipal MemberUserDetails userDetails,
            @RequestBody Map<String, String> request) {
        String password = request.get("password");

        boolean isSuccess = memberService.deleteMember(userDetails.getMemberId(), password);
        }
    }