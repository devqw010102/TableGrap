package com.example.demo.controller;

import com.example.demo.data.dto.MemberDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
@RequiredArgsConstructor
public class HomeController {

    @GetMapping("/")    // localhost:8080 접속 시 실행
    public String getHome(){
        return "index";
    }

    @GetMapping("/login")
    public String getLogin(){
        return "user/login";
    }

    @GetMapping("/register")
    public String getRegister(){
        return "user/register-choice";
    }

    @GetMapping("/register/user")
    public String getMemberAdd(@ModelAttribute("member")MemberDto memberDto){
        return "user/register";
    }

    @GetMapping("/ownerPage")
    public String getOwnerPage() { return "user/ownerPage"; }

    @GetMapping("/admin")   // 권한 끄면 들어가짐.(현재 login 페이지로)
    public String getAdmin() {
        return "admin/adminPage";
    }

    @GetMapping("/register/owner")
    public String getOwnerReg() { return "user/owner-register"; }

    @GetMapping("/mypage")
    public String getMyPage() { return "user/myPage"; }
}
