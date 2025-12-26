package com.example.demo.service.impl;

import com.example.demo.data.dto.MemberInfoResponseDto;
import com.example.demo.data.dto.admin.*;
import com.example.demo.data.model.*;
import com.example.demo.data.repository.*;
import com.example.demo.service.AdminService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminServiceImpl implements AdminService {

    private final DinerRepository dinerRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final AuthorityRepository authorityRepository;
    private final ReviewRepository reviewRepository;
    private final OwnerRequestRepository ownerRequestRepository;
    private final OwnerRepository ownerRepository;

    public Page<AdminDinerDto> getList(Pageable pageable) {
        return dinerRepository.findAll(pageable).map(AdminDinerDto::from);
    }

    @Override
    public Page<MemberInfoResponseDto> getMember(Pageable pageable) {
        return memberRepository.findAll(pageable).map(MemberInfoResponseDto::from);
    }

    @Transactional
    @Override
    public Page<AdminOwnerDto> getOwners(Pageable pageable) {
        return dinerRepository.findOwnerDiners(pageable).map(AdminOwnerDto::from);
    }

    @Override
    public Page<AdminBookDto> getBooks(Pageable pageable) {
        return bookRepository.findAll(pageable).map(AdminBookDto :: from);
    }

    @Transactional
    @Override
    public Page<AdminReviewDto> getReviews(Pageable pageable) {
        return reviewRepository.findAllForAdmin(pageable);
    }

    @Override
    public AdminDashboardDto getDashboard(Pageable pageable) {

        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        long dinerCount = dinerRepository.countAllDiners();
        long bookingCount = bookRepository.countTodayBookings(start, end);
        long memberCount = memberRepository.countMembersExceptAdmin() + ownerRepository.countOwnersExceptAdmin();

        Map<String, Long> roleMap = authorityRepository.countByRoleForDashboard()
                .stream()
                .collect(Collectors.toMap(
                        o -> (String) o[0],
                        o -> ((Number) o[1]).longValue()
                ));

        return new AdminDashboardDto(
                dinerCount,
                bookingCount,
                memberCount,
                roleMap.getOrDefault("ROLE_OWNER", 0L),
                roleMap.getOrDefault("ROLE_USER", 0L),
                reviewRepository.findTodayReviews(start, end, PageRequest.of(0, 5))
        );
    }
}
