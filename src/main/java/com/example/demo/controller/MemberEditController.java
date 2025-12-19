package com.example.demo.controller;

import com.example.demo.data.dto.MemberDto;
import com.example.demo.data.dto.MemberUpdateDto;
import com.example.demo.data.model.MemberUserDetails;
import com.example.demo.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/myPage")
@RequiredArgsConstructor
public class MemberEditController {

    private final MemberService memberService;

    // edit
    @GetMapping("/check-email")
    public String checkEmail(@RequestParam String email) {
        boolean isDuplicate = false;
        return isDuplicate ? "이미 사용중인 이메일입니다." : "사용 가능한 이메일입니다.";
    }

    @PostMapping("/update")
    public void updateMember(
            @AuthenticationPrincipal MemberUserDetails userDetails,
            @RequestBody MemberUpdateDto memberUpdateDto) {
        memberService.updateMember(userDetails.getMember().getId(), memberUpdateDto);

    }

    // delete
    @PostMapping("/delete")
    public ResponseEntity<?> deleteMember(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal MemberUserDetails userDetails,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        String password = request.get("password");

        try {
            memberService.deleteMember(userDetails.getMember().getId(), password);

            // 2. DB 삭제 성공 시에만 세션 및 보안 컨텍스트 초기화 (로그아웃)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                new SecurityContextLogoutHandler().logout(httpRequest, httpResponse, auth);
            }
            return ResponseEntity.ok().body("회원 탈퇴가 완료되었습니다.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}