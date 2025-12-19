package com.example.demo.service.impl;

import com.example.demo.data.dto.admin.AdminDashboardDto;
import com.example.demo.data.repository.*;
import com.example.demo.service.AdminService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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


    @Override
    public AdminDashboardDto getDashboard() {

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
                reviewRepository.findTodayReviews(start, end)
        );
    }
}
