package com.example.demo.service.impl;

import com.example.demo.data.dto.BookDto;
import com.example.demo.data.dto.BookResponseDto;
import com.example.demo.data.dto.notification.ReservationApproveEvent;
import com.example.demo.data.dto.notification.ReservationCancelEvent;
import com.example.demo.data.dto.notification.ReservationRejectEvent;
import com.example.demo.data.dto.owner.BookOwnerResponseDto;
import com.example.demo.data.model.Book;
import com.example.demo.data.model.Diner;
import com.example.demo.data.model.Member;
import com.example.demo.data.model.Review;
import com.example.demo.data.repository.BookRepository;
import com.example.demo.data.repository.DinerRepository;
import com.example.demo.data.repository.MemberRepository;
import com.example.demo.data.repository.ReviewRepository;
import com.example.demo.service.BookService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final DinerRepository dinerRepository;
    private final MemberRepository memberRepository;
    //review 수정 위해 reviewRepository 주입
    private final ReviewRepository reviewRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public List<BookResponseDto> findMyBooks(Long memberId) {
        return bookRepository.findByMember_id(memberId)
                .stream()
                .map(book -> {
                        //버튼 변환 구현을 위해 dto에 reviewId 넣었으며, entity는 건드리지 않음
                        Long reviewId = reviewRepository.findByBookId(book.getBookId())
                                        .map(Review::getReviewId)
                                        .orElse(null);
                        return  BookResponseDto.builder()
                            .bookId(book.getBookId())   //  bookResponse dto에
                            .dinerId(book.getDiner().getId()) //  bookResponse dto에
                            .dinerName(book.getDiner().getDinerName())
                            .bookingDate(book.getBookingDate())
                            .personnel(book.getPersonnel())
                            .memberName(book.getMember().getName())
                            .success(book.getSuccess())
                            .reviewId(reviewId)
                            .build();
                        })
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
        book.setSuccess(false);
    }

    @Override
    public void deleteBooking(Long bookId) {
        Book book = bookRepository.findById(bookId).orElseThrow();
        bookRepository.deleteById(bookId);

        // Owner Entity 들어오면 수정될수도 있음
        eventPublisher.publishEvent(new ReservationCancelEvent(
                book.getMember().getId(),
                book.getDiner().getDinerName(),
                book.getMember().getName(),
                book.getBookingDate()
        ));
    }

    @Override
    @Transactional
    public void createBooking(BookDto dto) {
            boolean exists = bookRepository.existsByMember_IdAndBookingDate(dto.getMemberId(), dto.getBookingDate());
            if (exists) {
                throw new IllegalArgumentException("날짜를 확인해주세요. 해당 날짜에 이미 예약이 존재합니다.");
            }

            Diner diner = dinerRepository.findById(dto.getDinerId())
                    .orElseThrow(() -> new IllegalArgumentException("식당을 찾을 수 없습니다."));

            Member member = memberRepository.findById(dto.getMemberId())
                    .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

            Book book = Book.builder()
                    .diner(diner)
                    .member(member)
                    .bookingDate(dto.getBookingDate())
                    .personnel(dto.getPersonnel())
                    .success(false)
                    .build();
            bookRepository.save(book);
    }

    @Override
    public Page<BookOwnerResponseDto> getBookings(Long ownerId, Long dinerId, Boolean pending, LocalDate date, int page, int size) {
        LocalDateTime start = date != null ? date.atStartOfDay() : null;
        LocalDateTime end = date != null ? date.plusDays(1).atStartOfDay() : null;
        Pageable pageable = PageRequest.of(page, size);
        return bookRepository.findBookings(ownerId, dinerId, pending, start, end, pageable);
    }

    @Override
    public void approveBooking(Long bookId) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new IllegalArgumentException("예약이 존재하지 않습니다."));

        if(Boolean.TRUE.equals(book.getSuccess())) {
            throw new IllegalStateException("이미 승인된 예약입니다.");
        }
        book.setSuccess(true);

        eventPublisher.publishEvent(new ReservationApproveEvent(
                book.getMember().getId(),     // 알림 받을 유저 ID
                book.getDiner().getDinerName(),     // 식당 이름
                book.getBookingDate()      // 예약 시간
        ));
    }

    @Override
    public void rejectBooking(Long bookId) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new IllegalArgumentException("예약이 존재하지 않습니다."));

        bookRepository.delete(book);

        eventPublisher.publishEvent(new ReservationRejectEvent(
                book.getMember().getId(),
                book.getDiner().getDinerName(),
                book.getBookingDate()
        ));
    }
}