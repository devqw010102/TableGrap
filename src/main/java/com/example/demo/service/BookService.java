package com.example.demo.service;

import com.example.demo.data.dto.BookResponseDto;
import com.example.demo.data.dto.MemberInfoResponseDto;
import com.example.demo.data.model.Book;
import com.example.demo.data.model.Member;

import java.util.List;

public interface BookService {
    List<BookResponseDto> findMyBooks(Long memberId);

    // owner 가 예약 확정 할 메소드
}
