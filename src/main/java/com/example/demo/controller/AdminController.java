package com.example.demo.controller;

import com.example.demo.data.dto.DinerDetailDto;
import com.example.demo.data.dto.MemberDto;
import com.example.demo.data.dto.MemberInfoResponseDto;
import com.example.demo.data.dto.OwnerRequestDto;
import com.example.demo.service.DinerExcelService;
import com.example.demo.service.DinerService;
import com.example.demo.service.MemberService;
import com.example.demo.service.OwnerRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/adminPage")
public class AdminController {

    private final OwnerRequestService ownerRequestService;
    private final DinerService dinerService;
    private final MemberService  memberService;

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
}
