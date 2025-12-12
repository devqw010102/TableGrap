package com.example.demo.controller;

//import ch.qos.logback.core.model.Model;
import com.example.demo.data.dto.MemberDto;
import com.example.demo.service.MemberService;
import jakarta.validation.Valid;
import lombok.Getter;
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

    @GetMapping
    public String viewIndex() {
        return  "index";
    }

    @GetMapping("/register")
    public String getMemberAdd(@ModelAttribute("member")MemberDto memberDto) {
        return "user/register";
    }

    @PostMapping("/register")
    public String postMemberAdd(@ModelAttribute("member") @Valid MemberDto memberDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {    // DTO 검증 결과 확인(@Size, @Pattern 등)
            return "user/register";
        }
        if (!memberDto.getPassword().equals(memberDto.getPasswordConfirm())) {
            bindingResult.rejectValue("passwordConfirm", "MissMatch", "입력하신 패스워드가 다릅니다.");
        }
        if (memberService.findByEmail(memberDto.getEmail()).isPresent()) {
            bindingResult.rejectValue("email", "AlreadyExist", "사용중인 이메일 입니다");
        }
        if (memberService.isUsernameDuplicate(memberDto.getUsername())) {
            bindingResult.rejectValue("userName", "AlreadyExist", "이미 사용중인 아이디 입니다");
        }
        if (bindingResult.hasErrors()) {
            return "user/register";
        }
        memberService.createMember(memberDto);
        return "redirect:/";
    }
//      HomeController 만들기 전 테스트용 추후 삭제 요망
//    @GetMapping("/login")
//    public String login() {
//        return "user/login";
//    }
//
//    @GetMapping("logout")
//    public String logout() {
//        return "user/logout";
//    }
}
