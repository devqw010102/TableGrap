package com.example.demo.service.impl;

import com.example.demo.data.dto.BookDto;
import com.example.demo.data.dto.BookResponseDto;
import com.example.demo.data.model.Book;
import com.example.demo.data.repository.BookRepository;
import com.example.demo.service.BookService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    @Override
    public List<BookResponseDto> findMyBooks(Long memberId) {
        return bookRepository.findByMember_id(memberId)
                .stream()
                .map(book -> BookResponseDto.builder()
                        .bookId(book.getBookId())   //  bookresponse dto에
                        .dinerId(book.getDiner().getId()) //  bookresponse dto에
                        .dinerName(book.getDiner().getDinerName())
                        .bookingDate(book.getBookingDate())
                        .personnel(book.getPersonnel())
                        .memberName(book.getMember().getName())
                        .success(book.getSuccess())
                        .build())
                .toList();
    }

    @Override
    public BookResponseDto getBooking(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("예약이 존재하지 않습니다."));

        // Entity -> DTO 변환
        return BookResponseDto.builder()
                .bookId(book.getBookId())
                .dinerId(book.getDiner().getId())
                .dinerName(book.getDiner().getDinerName())
                .bookingDate(book.getBookingDate())
                .personnel(book.getPersonnel())
                .build();
    }

    @Override
    @Transactional
    public void updateBooking(BookDto dto) {
        Book book = bookRepository.findById(dto.getBookId())
                .orElseThrow(() -> new IllegalArgumentException("예약이 존재하지 않습니다."));

        //update
        book.setBookingDate(dto.getBookingDate());
        book.setPersonnel(dto.getPersonnel());
    }

    @Override
    public void deleteBooking(Long bookId) {
        bookRepository.deleteById(bookId);
    }
}
