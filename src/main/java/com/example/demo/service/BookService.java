package com.example.demo.service;

import com.example.demo.data.dto.BookDto;
import com.example.demo.data.dto.BookResponseDto;
import com.example.demo.data.dto.owner.BookOwnerResponseDto;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public interface BookService {
    List<BookResponseDto> findMyBooks(Long memberId);
    BookResponseDto getBooking(Long bookId);
    void updateBooking(BookDto dto);

    void deleteBooking(Long bookId);

    void createBooking(BookDto bookDto);

    // owner 관련 메소드
    Page<BookOwnerResponseDto> getBookings(Long ownerId, Long dinerId, Boolean pending, LocalDate date, int page, int size);

    void approveBooking(Long bookId);

    void rejectBooking(Long bookId);
}