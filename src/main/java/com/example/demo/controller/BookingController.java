package com.example.demo.controller;

import com.example.demo.data.dto.BookDto;
import com.example.demo.data.dto.BookResponseDto;
import com.example.demo.data.dto.DinerDetailDto;
import com.example.demo.data.model.Member;
import com.example.demo.data.userDeatils.MemberUserDetails;
import com.example.demo.service.BookService;
import com.example.demo.service.DinerService;
import com.example.demo.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    private final DinerService dinerService;
    private final MemberService memberService;
    private final BookService bookService;

    // api
    @Value("${naver.client.id}")
    private String naverClientId;

    @GetMapping("/reservation")
    public String reservationPage(@RequestParam(value = "id", required = false) Long dinerId,
                                  @RequestParam(value = "bookId", required = false) Long bookId,
                                  Model model, Principal principal) {

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

        // 예약 수정
        if (bookId != null) {
            BookResponseDto myBooking = bookService.getBooking(bookId);

            model.addAttribute("myBooking", myBooking);
            model.addAttribute("mode", "modify"); //예약하기 버튼을 예약 수정 버튼으로 모드변경

        } else {
            model.addAttribute("mode", "new");  // 예약하기 버튼

        }
        return "reservation/reservation";
    }

    // 신규 예약
    @PostMapping("/book/save")
    public String saveBooking(
            @ModelAttribute BookDto bookDto,
            Principal principal
    ) {
        // 3. 로그인 정보가 있는지 확인하고 아이디(Username)를 DTO에 넣어줌
        if (principal != null) {
            String username = principal.getName();
            Member member = memberService.getMember(username);
            bookDto.setMemberId(member.getId());
        }

        try {
            bookService.createBooking(bookDto);
            return "redirect:/mypage";
        } catch (Exception e) {
            log.error("예약 실패 - bookId={}", bookDto.getBookId(), e);
            return "redirect:/reservation?error=true";
        }
    }

    // 예약 수정 저장
    @PostMapping("/book/update")
    public String updateBooking(@ModelAttribute BookDto bookDto,
                                @AuthenticationPrincipal MemberUserDetails userDetails) {

        System.out.println("=== 수정 요청 들어옴 ===");
        System.out.println("수정할 예약 번호(bookId): " + bookDto.getBookId());
        System.out.println("변경할 날짜: " + bookDto.getBookingDate());
        System.out.println("변경할 인원: " + bookDto.getPersonnel());

        if (userDetails != null) {
            bookDto.setMemberId(userDetails.getMember().getId());
        }

        try {
            bookService.updateBooking(bookDto);
            return "redirect:/mypage";
        } catch (Exception e) {
            log.error("예약 수정 실패 - bookId={}", bookDto.getBookId(), e);
            return "redirect:/reservation?error=true";
        }
    }

}

