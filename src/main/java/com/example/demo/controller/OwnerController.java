package com.example.demo.controller;

import com.example.demo.data.dto.owner.BookOwnerResponseDto;
import com.example.demo.data.dto.owner.OwnerDinerDto;
import com.example.demo.data.dto.owner.OwnerDto;
import com.example.demo.data.dto.owner.OwnerReviewDto;
import com.example.demo.data.model.MemberUserDetails;
import com.example.demo.service.BookService;
import com.example.demo.service.DinerService;
import com.example.demo.service.OwnerService;
import com.example.demo.service.ReviewService;
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

    @GetMapping("/reviews")
    public Page<OwnerReviewDto> getReviews(
            @AuthenticationPrincipal MemberUserDetails userDetails,
            @RequestParam(required = false) Long dinerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return reviewService.getOwnerReviews(userDetails.getMember(), dinerId, page, size);
    }

    //owner 회원가입
    @PostMapping("/register")
    public ResponseEntity<?>  register(@RequestBody OwnerDto ownerDto, BindingResult bindingResult) {
        if(bindingResult.hasErrors()){
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(","));
            return ResponseEntity.badRequest().body(errorMessage);
        }

        if (ownerService.existsByUsername(ownerDto.getUsername())) {
            return ResponseEntity.badRequest().body("Username is already in use");
        }

        if (ownerService.findByEmail(ownerDto.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email is already in use");
        }
        if (!ownerDto.getPassword().equals(ownerDto.getPasswordConfirm())) {
            return ResponseEntity.badRequest().body("Passwords do not match");
        }
        ownerService.createOwner(ownerDto);
        return ResponseEntity.ok("회원 가입이 완료되었습니다.");
    }

    // Email double check
    @GetMapping("/check-email")
    public String checkEmail(@RequestParam String email) {
        boolean isDuplicate = ownerService.findByEmail(email).isPresent();
        return isDuplicate ? "이미 사용 중인 이메일입니다." : "사용 가능한 이메일입니다.";
    }

    // ID double check
    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
        return ResponseEntity.ok(ownerService.existsByUsername(username));
    }
}
