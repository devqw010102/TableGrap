package com.example.demo.service.impl;

import com.example.demo.data.dto.BookResponseDto;
import com.example.demo.data.model.Book;
import com.example.demo.data.model.Member;
import com.example.demo.data.repository.BookRepository;
import com.example.demo.service.BookService;
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
                        .dinerName(book.getDiner().getDinerName())
                        .bookingDate(book.getBookingDate())
                        .personnel(book.getPersonnel())
                        .memberName(book.getMember().getName())
                        .success(book.getSuccess())
                        .build())
                .toList();
    }
}
