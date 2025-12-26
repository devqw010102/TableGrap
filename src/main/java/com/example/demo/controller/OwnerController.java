package com.example.demo.controller;

import com.example.demo.data.dto.DinerDto;
import com.example.demo.data.dto.owner.*;
import com.example.demo.data.model.OwnerUserDetails;
import com.example.demo.service.BookService;
import com.example.demo.service.DinerService;
import com.example.demo.service.OwnerService;
import com.example.demo.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    //Owner계정 회원 정보 출력
    @GetMapping("/info")
    public Optional<OwnerDto> getOwnerInfo(@AuthenticationPrincipal OwnerUserDetails userDetails) {
        return ownerService.findByOwnerId(userDetails.getOwner().getId());
    }

    //Owner 회원 정보 수정
    @PatchMapping("/update")
    @Transactional
    public void updateOwner(
            @AuthenticationPrincipal OwnerUserDetails userDetails,
            @RequestBody OwnerUpdateDto updateDto) {
        ownerService.updateOwner(userDetails.getOwner().getId(), updateDto);
    }

    // 식당 추가
    @PatchMapping("/add/diner")
    @Transactional
    public void addDiner(@RequestBody DinerDto dto, Principal principal) {
        dinerService.addDiner(dto, principal.getName());
    }

    //식당 삭제 탭에서 식당 출력
    @GetMapping("/diner/{dinerId}")
    public ResponseEntity<OwnerDinerDto> getOwnerDinerById(
            @PathVariable Long dinerId,
            @AuthenticationPrincipal OwnerUserDetails userDetails
    ) {
        return dinerService.getOwnerDinerById(dinerId, userDetails.getOwner().getId())
                .map(ResponseEntity::ok) // 찾았으면 200 OK + 데이터
                .orElse(ResponseEntity.notFound().build()); // 없으면 404 Not Found
    }
    //식당 삭제
    @DeleteMapping("/delete/diner/{dinerId}")
    @Transactional
    public ResponseEntity<String> deleteOwnerDiner(@PathVariable("dinerId") Long dinerId,  @AuthenticationPrincipal OwnerUserDetails userDetails) {
        try{
            dinerService.deleteDiner(dinerId, userDetails.getOwner().getId());
            return ResponseEntity.ok("식당이 삭제되었습니다.");
        } catch (IllegalStateException e) {
            // 예약이 남아있어 삭제 못하는 경우 (400 Bad Request)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            // 식당이 없거나 권한이 없는 경우 (404 Not Found)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    //계정 삭제
    @DeleteMapping("/delete/owner")
    @Transactional
    public ResponseEntity<?> deleteOwner(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal OwnerUserDetails userDetails,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        String password = request.get("password");

        try{
            ownerService.deleteOwner(userDetails.getOwner().getId(), password);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if(auth != null) {
                new SecurityContextLogoutHandler().logout(httpRequest, httpResponse, auth);
            }
            return ResponseEntity.ok().body("회원탈퇴가 완료되었습니다.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
