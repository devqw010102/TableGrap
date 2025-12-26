package com.example.demo.service;

import com.example.demo.data.dto.MemberInfoResponseDto;
import com.example.demo.data.dto.admin.*;
import com.example.demo.data.dto.owner.OwnerRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminService {

    Page<AdminDinerDto> getList(Pageable pageable);

    Page<MemberInfoResponseDto> getMember(Pageable pageable);

    Page<AdminOwnerDto> getOwners(Pageable pageable);

    Page<OwnerRequestDto> findAllByStatus(Pageable pageable);

    void approve(Long requestId);

    void reject(Long requestId);

    Page<AdminBookDto> getBooks(Pageable pageable);

    Page<AdminReviewDto> getReviews(Pageable pageable);

    AdminDashboardDto getDashboard(Pageable pageable);

}
