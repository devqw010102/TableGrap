package com.example.demo.service;

import com.example.demo.data.dto.MemberInfoResponseDto;
import com.example.demo.data.dto.admin.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface AdminService {

    Page<AdminDinerDto> getList(Pageable pageable);

    Page<MemberInfoResponseDto> getMember(Pageable pageable);

    Page<AdminOwnerDto> getOwners(Pageable pageable);

    Page<AdminBookDto> getBooks(Pageable pageable);

    Page<AdminReviewDto> getReviews(Pageable pageable);

    AdminDashboardDto getDashboard(Pageable pageable);

    List<Map<String, Object>> getCategoryStats();

    List<Map<String, Object>> getWeeklyReservationChart();
}
