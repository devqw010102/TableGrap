package com.example.demo.service.impl;

import com.example.demo.data.dto.MemberInfoResponseDto;
import com.example.demo.data.dto.admin.*;
import com.example.demo.data.enums.AccountStatus;
import com.example.demo.data.enums.AuthorityStatus;
import com.example.demo.data.enums.DinerStatus;
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
import java.time.LocalTime;
import java.util.List;
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
        return dinerRepository.findOwnerDiners(pageable, DinerStatus.DELETED, AuthorityStatus.ROLE_OWNER.name()).map(AdminOwnerDto::from);
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

        long memberCount = memberRepository.countMembersExceptAdmin(AuthorityStatus.ROLE_ADMIN.name()) + ownerRepository.countOwnersExceptAdminAndDeleted(AuthorityStatus.ROLE_ADMIN.name(), AuthorityStatus.ROLE_DELETED.name());
        if(memberRepository.existsByUsername("unknown_user")) {
            memberCount -= 1;
        }

        Map<String, Long> roleMap = authorityRepository.countByRoleForDashboard(AuthorityStatus.ROLE_ADMIN.name(), AccountStatus.DELETED.name())
                .stream()
                .collect(Collectors.toMap(
                        o -> (String) o[0],
                        o -> ((Number) o[1]).longValue()
                ));

        return new AdminDashboardDto(
                dinerCount,
                bookingCount,
                memberCount,
                roleMap.getOrDefault(AuthorityStatus.ROLE_OWNER.name(), 0L),
                roleMap.getOrDefault(AuthorityStatus.ROLE_USER.name(), 0L),
                reviewRepository.findTodayReviews(start, end, PageRequest.of(0, 5))
        );
    }

    // 카테고리 차트
    @Override
    public List<Map<String, Object>> getCategoryStats() {
        return dinerRepository.findCategoryStats();
    }

    // 최근 1주일 예약 차트
    @Override
    public List<Map<String, Object>> getWeeklyReservationChart() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7).with(LocalTime.MIN);
        return bookRepository.getWeeklyBookingStats(startDate);
    }
}
