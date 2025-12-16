package com.example.demo.service.impl;

import com.example.demo.data.dto.BookDto;
import com.example.demo.data.model.Book;
import com.example.demo.data.model.Diner;
import com.example.demo.data.model.Member;
import com.example.demo.data.repository.BookRepository;
import com.example.demo.data.repository.DinerRepository;
import com.example.demo.data.repository.MemberRepository;
import com.example.demo.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookRepository bookRepository;
    private final DinerRepository dinerRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public Book createBooking(BookDto dto) {
        try {
            Diner diner = dinerRepository.findById(dto.getDiner())
                    .orElseThrow(() -> new IllegalArgumentException("식당을 찾을 수 없습니다."));

            Member member = memberRepository.findByUsername(dto.getMemberId())
                    .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

            Book book= Book.builder()
                    .diner(diner)
                    .member(member)
                    .bookingDate(dto.getBookingDate())
                    .personnel(dto.getPersonnel())
                    .build();
            return bookRepository.save(book);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("예약 처리 중 오류가 발생했습니다.");
        }
    }
}