package com.example.demo.controller;

import ch.qos.logback.core.model.Model;
import com.example.demo.data.dto.MemberDto;
import com.example.demo.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("user/register")
    public String getMemberAdd(@ModelAttribute("member")MemberDto memberDto) {
        return "user/register";
    }

    @PostMapping("user/register")
    public String postMemberAdd(@ModelAttribute("member")MemberDto memberDto, BindingResult bindingResult) {
        if (memberDto.getPassword() == null || memberDto.getPassword().length() < 8) {
            bindingResult.rejectValue("password", "NotBlank", "패스워드를 8글자 이상 입력하세요");
        }
        if (!memberDto.getPassword().equals(memberDto.getPasswordConfirm())) {
            bindingResult.rejectValue("passwordConfirm", "MissMatch", "입력하신 패스워드가 다릅니다.");
        }
        if (memberService.findByEmail(memberDto.getEmail()).isPresent()) {
            bindingResult.rejectValue("email", "AlreadyExist", "사용중인 이메일 입니다");
        }
        if (memberService.isUsernameDuplicate(memberDto.getUserName())) {
            bindingResult.rejectValue("userName", "AlreadyExist", "이미 사용중인 아이디 입니다");
        }
        if (bindingResult.hasErrors()) {
            return "user/register";
        }
        memberService.createMember(memberDto);
        return "redirect:/";
    }
}
