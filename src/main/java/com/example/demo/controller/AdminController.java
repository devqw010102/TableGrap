package com.example.demo.controller;

import com.example.demo.data.dto.*;
import com.example.demo.data.dto.admin.AdminBookDto;
import com.example.demo.data.dto.admin.AdminDashboardDto;
import com.example.demo.data.dto.admin.AdminOwnerDto;
import com.example.demo.data.dto.admin.AdminReviewDto;
import com.example.demo.data.dto.owner.OwnerRequestDto;
import com.example.demo.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/adminPage")
public class AdminController {

    private final OwnerRequestService ownerRequestService;
    private final DinerService dinerService;
    private final MemberService  memberService;
    private final BookService bookService;
    private final ReviewService reviewService;
    private final AdminService adminService;

    // 식당 목록 fetch
    @GetMapping("/diners")
    public List<DinerDetailDto> diners() {
        return dinerService.getList();
    }

    // 회원 목록 fetch
    @GetMapping("/members")
    public List<MemberInfoResponseDto> members() {
        return memberService.getList();
    }

    // 권한 신청 목록 fetch
    @GetMapping("/owner-requests")
    public List<OwnerRequestDto> ownerRequests() {
        return ownerRequestService.findAll();
    }

    // 권한 승인
    @PutMapping("/owner-requests/{id}/approve")
    public void approveOwner(@PathVariable Long id) {
        ownerRequestService.approve(id);
    }

    // 권한 반려
    @PutMapping("/owner-requests/{id}/reject")
    public void rejectOwner(@PathVariable Long id) {
        ownerRequestService.reject(id);
    }

    // 예약 목록 fetch
    @GetMapping("/books")
    public List<AdminBookDto> books() {
        return bookService.getAll();
    }

    @GetMapping("/owners")
    public List<AdminOwnerDto> owners() {
        return dinerService.getAll();
    }

    @GetMapping("/reviews")
    public List<AdminReviewDto> reviews() {
        return reviewService.getAll();
    }

    @GetMapping("/dashboard")
    public AdminDashboardDto dashboard() {
        return adminService.getDashboard();
    }
}
