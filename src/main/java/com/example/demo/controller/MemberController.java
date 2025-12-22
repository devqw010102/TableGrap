package com.example.demo.controller;

import com.example.demo.data.dto.MemberInfoResponseDto;
import com.example.demo.data.dto.MemberUpdateDto;
import com.example.demo.data.model.MemberUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import com.example.demo.data.dto.MemberDto;
import com.example.demo.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor

// MemberController + MemberEditController RestController로 합치기
public class MemberController {

    private final MemberService memberService;

    // Home
    @GetMapping
    public String viewIndex() {
        return "index";
    }

    // Member create - register
    @PostMapping ("/register")
    public ResponseEntity<?> register(@RequestBody @Valid MemberDto memberDto, BindingResult bindingResult) {

        if(bindingResult.hasErrors()){
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(","));
            return ResponseEntity.badRequest().body(errorMessage);
    }

        if (memberService.isUsernameDuplicate(memberDto.getUsername())) {
        return ResponseEntity.badRequest().body("Username is already in use");
        }

        if (memberService.findByEmail(memberDto.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email is already in use");
        }
        if (!memberDto.getPassword().equals(memberDto.getPasswordConfirm())) {
            return ResponseEntity.badRequest().body("Passwords do not match");
        }
        memberService.createMember(memberDto);
        return ResponseEntity.ok("회원 가입이 완료되었습니다.");
    }

    // Email double check
    @GetMapping("/check-email")
    public String checkEmail(@RequestParam String email) {
        boolean isDuplicate = memberService.findByEmail(email).isPresent();
        return isDuplicate ? "이미 사용 중인 이메일입니다." : "사용 가능한 이메일입니다.";
    }

    // ID double check
    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
        return ResponseEntity.ok(memberService.isUsernameDuplicate(username));
    }

    // Member 조회
    @GetMapping("/info")
    public ResponseEntity<MemberInfoResponseDto> getMyInfo(@AuthenticationPrincipal MemberUserDetails userDetails) {
        return ResponseEntity.ok(memberService.findMyInfo(userDetails.getMember().getId()));
    }

    // Member edit
    @PostMapping("/update")
    public void updateMember(
            @AuthenticationPrincipal MemberUserDetails userDetails,
            @RequestBody MemberUpdateDto memberUpdateDto) {
        memberService.updateMember(userDetails.getMember().getId(), memberUpdateDto);

    }

    // Member delete
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