package com.example.demo.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemberViewController {
    @GetMapping("/member/register")
    public String registerPage() {
        return "user/register";
    }

    @GetMapping("/member/myPage")
    public String memberPage() {
        return "user/myPage";
    }

    // 아이디 찾기 페이지 이동
    @GetMapping("/member/findId")
    public String findIdPage() {
        return "user/findId";
    }

    // 비밀번호 재설정 페이지 이동
    @GetMapping("/member/resetPwd")
    public String resetPwdPage() {
        return "user/resetPwd";
    }
}
