package com.example.demo.service.impl;

import com.example.demo.data.dto.MemberInfoResponseDto;
import com.example.demo.data.dto.admin.*;
import com.example.demo.data.dto.owner.OwnerRequestDto;
import com.example.demo.data.enums.RequestStatus;
import com.example.demo.data.model.Authority;
import com.example.demo.data.model.Diner;
import com.example.demo.data.model.Member;
import com.example.demo.data.model.OwnerRequest;
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

    // 권한 신청 목록
    @Override
    public Page<OwnerRequestDto> findAllByStatus(Pageable pageable) {
        return ownerRequestRepository.findAllByStatus(RequestStatus.PENDING, pageable).map(OwnerRequestDto::from);
    }

    // 승인 처리
    @Override
    public void approve(Long requestId) {
        OwnerRequest request = ownerRequestRepository.findById(requestId).orElseThrow(() -> new IllegalArgumentException("not found request"));

        if(request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 신청입니다.");
        }

        Diner diner = request.getDiner();
        Member member = request.getMember();

        if(diner.getOwner() != null) {
            throw new IllegalStateException("이미 사장이 등록된 식당입니다.");
        }

        // Status 승인으로, Diner entity 에도 owner 값 추가
        request.setStatus(RequestStatus.APPROVED);
        diner.setOwner(member);

        // Authority 에 해당 아이디가 'ROLE_OWNER'를 가지고 있다면 추가 X
        boolean hasOwnerRole = authorityRepository.existsByMemberAndAuthority(member, "ROLE_OWNER");
        if(!hasOwnerRole) {
            Authority ownerAuthority = Authority.builder()
                    .member(member)
                    .authority("ROLE_OWNER")
                    .build();

            authorityRepository.save(ownerAuthority);
        }
    }

    // 반려 처리
    @Override
    public void reject(Long requestId) {
        OwnerRequest request = ownerRequestRepository.findById(requestId).orElseThrow(() -> new IllegalArgumentException("not found request"));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 신청입니다.");
        }

        request.setStatus(RequestStatus.REJECTED);
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
        long memberCount = memberRepository.countMembersExceptAdmin();

        Map<String, Long> roleMap = authorityRepository.countByAuthority()
                .stream()
                .collect(Collectors.toMap(
                        o -> (String) o[0],
                        o -> (Long) o[1]
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
