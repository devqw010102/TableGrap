package com.example.demo.controller;

import com.example.demo.data.dto.BookResponseDto;
import com.example.demo.data.model.MemberUserDetails;
import com.example.demo.service.BookService; // BookingService가 아님!
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // 여기가 중요합니다 (스프링 모델)
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor

public class MyPageController {
    private final BookService bookService;

    // 예약 들고와서 list로 mypage에 뿌림
    @GetMapping("/mypage")
    public String myPage(Model model, @AuthenticationPrincipal MemberUserDetails userDetails) {
        if (userDetails != null) {
            Long memberId = userDetails.getMemberId();
            List<BookResponseDto> myBookingList = bookService.findMyBooks(memberId);
            model.addAttribute("list", myBookingList);
        }
        return "user/myPage";
    }
}
