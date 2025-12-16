package com.example.demo.controller;

import com.example.demo.data.dto.MemberDto;
import com.example.demo.service.DinerService;
import com.example.demo.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.beans.BeanInfo;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final MemberService memberService;
    private final DinerService dinerService;

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


        return isOwner ? "user/ownerPage" : "user/myPage";
    }

    @GetMapping("/admin")   // 권한 끄면 들어가짐.(현재 login 페이지로)
    public String getAdmin(Model model) {
        model.addAttribute("diner", dinerService.getList());
        model.addAttribute("member", memberService.getList());
        return "admin/adminPage";
    }

    /* 충돌남ㅜ
    @GetMapping("/reservation")
    public String getReservation(){
        return "reservation/reservation";
    }

     */
    // 테스트용 로그인한 일반회원이 예약하기 버튼을 누르면 mypage로 가게 수정하기
    @GetMapping("/myPage")
    public String simpleMyPage() {
        return "user/myPage";
    }
}
