package com.example.demo.controller;

import com.example.demo.data.dto.MemberDto;
import com.example.demo.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.beans.BeanInfo;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final MemberService memberService;

    @GetMapping("/")    // localhost:8080 접속 시 실행
    public String getHome(){
        return "index";
    }

    @GetMapping("/login")
    public String getLogin(){
        return "user/login";
    }

    @GetMapping("/register")
    public String getMemberAdd(@ModelAttribute("member")MemberDto memberDto){
        return "user/register";
    }

    @GetMapping("/mypage")
    public String getMypage(@AuthenticationPrincipal UserDetails userDetails) {
        // 로그인 안했으면 로그인으로
        if (userDetails == null) {
            return "redirect:/login";
        }
        // 권한 확인( 일반 사용자: 마이페이지 / 가게 오너: 오너 페이지)
        boolean isOwner = userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_OWNER"));

    /* boolean isOwner = false;// 테스트용

        System.out.println("테스트 중: isOwner = " + isOwner); */

        if (isOwner) {
            return "user/ownerPage";
        } else {
            return "user/myPage";
        }
    }

    @GetMapping("/admin")   // 권한 끄면 들어가짐.(현재 login 페이지로)
    public String getAdmin(){
        return "admin/adminPage";
    }

    @GetMapping("/reservation")
    public String getReservation(){
        return "reservation/reservation";
    }
}
