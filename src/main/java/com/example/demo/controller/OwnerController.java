package com.example.demo.controller;

import com.example.demo.data.dto.BookOwnerResponseDto;
import com.example.demo.data.dto.OwnerDinerDto;
import com.example.demo.data.model.MemberUserDetails;
import com.example.demo.service.BookService;
import com.example.demo.service.DinerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owner")
@PreAuthorize("hasRole('OWNER')")
public class OwnerController {

    private final BookService bookService;
    private final DinerService dinerService;

    @GetMapping
    public Page<BookOwnerResponseDto> getBookings(
            @AuthenticationPrincipal MemberUserDetails userDetails,
            @RequestParam(required = false) Long dinerId,
            @RequestParam(required = false) Boolean pending,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return bookService.getBookings(userDetails.getMember().getId(), dinerId, pending, date, page, size);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<Void> approve(@PathVariable Long id) {
        bookService.approveBooking(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Void> reject(@PathVariable Long id) {
        bookService.rejectBooking(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/diners")
    public List<OwnerDinerDto> myDiners(@AuthenticationPrincipal MemberUserDetails userDetails) {
        return dinerService.getOwnerDiners(userDetails.getMember().getId());
    }

}
