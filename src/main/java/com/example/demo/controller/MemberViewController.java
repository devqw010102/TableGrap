package com.example.demo.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemberViewController {
    @GetMapping("/member/register")
    public String registerPage()
    {return "user/register";}

    @GetMapping("/member/myPage")
    public String memberPage()
    {return "user/myPage";}
}
