package com.example.demo.controller;

import com.example.demo.data.dto.BookDto;
import com.example.demo.data.dto.DinerDetailDto;
import com.example.demo.data.dto.MemberDto;
import com.example.demo.data.model.Diner;
import com.example.demo.data.model.Member;
import com.example.demo.data.model.MemberUserDetails;
import com.example.demo.service.BookingService;
import com.example.demo.service.DinerService;
import com.example.demo.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class BookingController {
    private final DinerService dinerService;
    private final MemberService memberService;
    private final BookingService bookingService;


    // api
    @Value("${naver.client.id}")
    private String naverClientId;

    @GetMapping("/reservation")
    public String reservationPage(@RequestParam(value = "id", required = false) Long dinerId, Model model, Principal principal) {
        if (principal == null) {      // 로그인한 사용자만 가능
            return "redirect:/login";
        }

        // ID가 없으면 기본값 1번 식당 보여주기...? 404?
        if (dinerId == null) {
            dinerId = 1L;
        }
        // DB에서 식당 정보 가져오기
        DinerDetailDto diner = dinerService.getDinerById(dinerId);

        // 예약자 정보 가져오기
        String username = principal.getName();
        Member member = memberService.getMember(username);


        // HTML로 데이터 보내기
        model.addAttribute("diner", diner);
        model.addAttribute("dinerId", dinerId);
        model.addAttribute("member", member);
        model.addAttribute("naverClientId", naverClientId); // API 키도

        return "reservation/reservation";
    }

    @PostMapping("/book/save")
    public String saveBooking(
            @ModelAttribute BookDto bookDto,
            Principal principal
    ) {
        // 3. 로그인 정보가 있는지 확인하고 아이디(Username)를 DTO에 넣어줌
        if (principal != null) {
            String username = principal.getName();
            bookDto.setMemberId(username);
        }

        System.out.println("컨트롤러로 들어온 데이터: " + bookDto);

        try{

            bookingService.createBooking(bookDto);
            return "redirect:/myPage";
        } catch(Exception e){
            e.printStackTrace();
            return "redirect:/reservation?error=true";
        }
    }
}
