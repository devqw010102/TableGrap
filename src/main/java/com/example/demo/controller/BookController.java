package com.example.demo.controller;

import com.example.demo.data.dto.BookDto;
import com.example.demo.data.dto.BookResponseDto;
import com.example.demo.data.dto.MemberInfoResponseDto;
import com.example.demo.data.dto.SlotResponseDto;
import com.example.demo.data.userDeatils.MemberUserDetails;
import com.example.demo.service.BookService;
import com.example.demo.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
    @PostMapping("/reservation")
    public ResponseEntity<?> reservation(BookDto dto, @AuthenticationPrincipal MemberUserDetails userDetails) {
        try {
            dto.setMemberId(userDetails.getMember().getId());

            if (dto.getBookId() != null) {
                bookService.updateBooking(dto);
                return ResponseEntity.ok("예약 수정 되었습니다.");
            } else {
            bookService.createBooking(dto);
            return ResponseEntity.ok("예약 완료되었습니다.");
        }
        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("예약 처리 중 서버 오류가 발생했습니다.");
        }
    }

    @GetMapping("/availability")
    public ResponseEntity<List<SlotResponseDto>> getAvailability(
            @RequestParam Long dinerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam int personnel) {

        // Service를 호출하여 가공된 14개 슬롯 리스트를 가져옴
        List<SlotResponseDto> slots = bookService.getDailyAvailability(date, personnel, dinerId);

        return ResponseEntity.ok(slots);
    }
}
