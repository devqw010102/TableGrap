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

    @GetMapping("/ownerPage")
    public String getOwnerPage() { return "user/ownerPage"; }

    @GetMapping("/admin")   // 권한 끄면 들어가짐.(현재 login 페이지로)
    public String getAdmin() {
        return "admin/adminPage";
    }

}
