package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.example.demo.data.dto.MemberDto;
import com.example.demo.data.dto.MemberInfoResponseDto;
import com.example.demo.data.dto.MemberUpdateDto;
import com.example.demo.data.model.MemberUserDetails;
import com.example.demo.service.MemberService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    /*
    // update
    @GetMapping("/update")
    public String updateForm(@AuthenticationPrincipal MemberUserDetails memberUserDetails, Model model) {
        MemberInfoResponseDto myInfo = memberService.findMyInfo(memberUserDetails.getMemberId());

        MemberUpdateDto updateMemberDto = MemberUpdateDto.builder()
                .email(myInfo.getEmail())
                .phone(myInfo.getPhone())
                .build();

        model.addAttribute("memberUpdateDto", updateMemberDto);
        return "user/myPage";
    }

    @PostMapping("/update")
    public String updateMember(
            @AuthenticationPrincipal MemberUserDetails userDetails,
            @ModelAttribute("memberUpdateDto") MemberUpdateDto memberUpdateDto,
            BindingResult bindingResult
    ) {
        // 비밀번호 수정 시, 비밀번호 확인 일치 여부 검사
        if (memberUpdateDto.getPassword() != null && !memberUpdateDto.getPassword().isEmpty()) {
            if (!memberUpdateDto.getPassword().equals(memberUpdateDto.getPasswordConfirm())) {
                bindingResult.rejectValue("passwordConfirm", "MissMatch", "비밀번호가 일치하지 않습니다.");
                return "/user/myPage";
            }
        }
        memberService.updateMember(userDetails.getMemberId(), memberUpdateDto);

        return "redirect:/user/myPage";
    }

    // delete
    @PostMapping("/delete")
    public String deleteMember(
            @AuthenticationPrincipal MemberUserDetails userDetails,
            @RequestParam("password") String password,
            RedirectAttributes redirectAttributes
    )
    {
        boolean isSuccess = memberService.deleteMember(userDetails.getMemberId(), password);

        if (isSuccess) {
            return "redirect:/logout";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "비밀번호가 일치하지 않아 탈퇴에 실패했습니다.");
            return "redirect:/member/myPage";
        }
    }
    */
}
