package com.example.demo.controller;

import com.example.demo.common.python.PythonProcessExecutor;
import com.example.demo.data.dto.MemberInfoResponseDto;
import com.example.demo.data.dto.admin.*;
import com.example.demo.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/adminPage")
public class AdminController {

    private final AdminService adminService;
    private final PythonProcessExecutor pythonProcessExecutor;

    // 식당 목록 fetch
    @GetMapping("/diners")
    public Page<AdminDinerDto> diners(@PageableDefault(size = 10, sort = "id") Pageable pageable) { return adminService.getList(pageable); }

    // 회원 목록 fetch
    @GetMapping("/members")
    public Page<MemberInfoResponseDto> members(@PageableDefault(size = 10, sort = "id") Pageable pageable) { return adminService.getMember(pageable); }

    // 예약 목록 fetch
    @GetMapping("/books")
    public Page<AdminBookDto> books(@PageableDefault(size = 10, sort = "bookId") Pageable pageable) {
        return adminService.getBooks(pageable);
    }

    // 사장 목록 fetch
    @GetMapping("/owners")
    public Page<AdminOwnerDto> owners(@PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return adminService.getOwners(pageable);
    }

    // 리뷰 목록 fetch
    @GetMapping("/reviews")
    public Page<AdminReviewDto> reviews(@PageableDefault(size = 10, sort = "reviewId") Pageable pageable) {
        return adminService.getReviews(pageable);
    }

    // 대시보드 fetch
    @GetMapping("/dashboard")
    public AdminDashboardDto dashboard(@PageableDefault(size = 5) Pageable pageable) {
        return adminService.getDashboard(pageable);
    }

    // 카테고리 차트
    @GetMapping("/charts/diner-categories")
    public String getDinerCategoryChart() {
        try {
            // 1. DB 값 불러오기
            List<Map<String, Object>> stats = adminService.getCategoryStats();

            // 2. Mapper 사용하여 Json 으로 변환
            ObjectMapper mapper = new ObjectMapper();
            String jsonData = mapper.writeValueAsString(stats);

            System.out.println("jsonData: " + jsonData);
            // 3. 파이썬 실행
            return pythonProcessExecutor.execute("admin", "category_donut_chart", jsonData);
        }
        catch(Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }
}
