package com.example.demo.controller;

import com.example.demo.data.dto.owner.*;
import com.example.demo.data.model.MemberUserDetails;
import com.example.demo.data.model.OwnerUserDetails;
import com.example.demo.service.BookService;
import com.example.demo.service.DinerService;
import com.example.demo.service.OwnerService;
import com.example.demo.service.ReviewService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owner")
@PreAuthorize("hasRole('OWNER')")
public class OwnerController {

    private final BookService bookService;
    private final DinerService dinerService;
    private final ReviewService reviewService;
    private final OwnerService ownerService;

    @GetMapping
    public Page<BookOwnerResponseDto> getBookings(
            @AuthenticationPrincipal OwnerUserDetails userDetails,
            @RequestParam(required = false) Long dinerId,
            @RequestParam(required = false) Boolean pending,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return bookService.getBookings(userDetails.getOwner().getId(), dinerId, pending, date, page, size);
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
    public List<OwnerDinerDto> myDiners(@AuthenticationPrincipal OwnerUserDetails userDetails) {
        return dinerService.getOwnerDiners(userDetails.getOwner().getId());
    }

    @GetMapping("/reviews")
    public Page<OwnerReviewDto> getReviews(
            @AuthenticationPrincipal OwnerUserDetails userDetails,
            @RequestParam(required = false) Long dinerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return reviewService.getOwnerReviews(userDetails.getOwner(), dinerId, page, size);
    }

    @GetMapping("/info")
    public Optional<OwnerDto> getOwnerInfo(@AuthenticationPrincipal OwnerUserDetails userDetails) {
        return ownerService.findByOwnerId(userDetails.getOwner().getId());
    }

    @PatchMapping("/update")
    @Transactional
    public void updateOwner(
            @AuthenticationPrincipal OwnerUserDetails userDetails,
            @RequestBody OwnerUpdateDto updateDto) {
        ownerService.updateOwner(userDetails.getOwner().getId(), updateDto);
    }
}
