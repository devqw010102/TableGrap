package com.example.demo.controller;

import com.example.demo.data.dto.BookResponseDto;
import com.example.demo.data.dto.MemberInfoResponseDto;
import com.example.demo.data.model.MemberUserDetails;
import com.example.demo.service.BookService;
import com.example.demo.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/myPage")
public class BookController {

    private final BookService bookService;
    private final MemberService memberService;

    @GetMapping("/books")
    public List<BookResponseDto> myBooks(@AuthenticationPrincipal MemberUserDetails userDetails) {
        Long memberId = userDetails.getMember().getId();
        return bookService.findMyBooks(memberId);
    }

    @GetMapping("/info")
    public MemberInfoResponseDto myInfo(@AuthenticationPrincipal MemberUserDetails userDetails) {
        return memberService.findMyInfo(userDetails.getMember().getId());
    }
    @DeleteMapping("/book/delete/{bookId}")
    public void deleteBooking(@PathVariable Long bookId) {
            bookService.deleteBooking(bookId);

    }
}
