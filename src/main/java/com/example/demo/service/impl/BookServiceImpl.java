package com.example.demo.service.impl;

import com.example.demo.data.dto.BookDto;
import com.example.demo.data.dto.BookResponseDto;
import com.example.demo.data.dto.SlotResponseDto;
import com.example.demo.data.dto.notification.*;
import com.example.demo.data.dto.owner.BookOwnerResponseDto;
import com.example.demo.data.model.*;
import com.example.demo.data.repository.*;
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
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final DinerAvailabilityRepository availabilityRepository;
    private ChronoLocalDateTime<?> limit;

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

        checkDuplicateBooking(book.getMember().getId(), dto.getBookingDate(), dto.getBookId());

        // 슬롯
        // 1. 기존 슬롯 복구
        availabilityRepository.findByDinerIdAndDateAndTime(book.getDiner().getId(), book.getBookingDate().toLocalDate(), book.getBookingDate().toLocalTime())
                .ifPresent(availability -> availability.addCapacity(book.getPersonnel()));

        // 2. 새로운 슬롯 확보
        LocalDate date = dto.getBookingDate().toLocalDate();
        LocalTime time = dto.getBookingDate().toLocalTime();

        DinerAvailability availability = availabilityRepository.findByDinerIdAndDateAndTime(book.getDiner().getId(), date, time).orElseGet(() ->
                availabilityRepository.save(
                        DinerAvailability.builder()
                                .diner(book.getDiner())
                                .date(date)
                                .time(time)
                                .maxCapacity(book.getDiner().getDefaultMaxCapacity() != null ? book.getDiner().getDefaultMaxCapacity() : 10)
                                .currentCapacity(book.getDiner().getDefaultMaxCapacity() != null ? book.getDiner().getDefaultMaxCapacity() : 10)
                                .build()
                ));

        availability.removeCapacity(book.getPersonnel());

        //update
        book.setBookingDate(dto.getBookingDate());
        book.setPersonnel(dto.getPersonnel());
        book.setSuccess(false);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formatterDate = book.getBookingDate().format(dtf);

        eventPublisher.publishEvent(new ReservationUpdateEvent(
                book.getDiner().getOwner().getId(),
                book.getDiner().getDinerName(),
                formatterDate
        ));
    }

    // cancel booking
    @Override
    public void deleteBooking(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("예약 정보를 찾을 수 없습니다."));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime bookingDate = book.getBookingDate();
        LocalDateTime limit = bookingDate.minusHours(24);

        boolean isAllowedByOwner = OwnerServiceImpl.CancelManager.allowedBookingIds.contains(bookId);

        if(Boolean.TRUE.equals(book.getSuccess()) && now.isAfter(limit) && !isAllowedByOwner) {
            throw new RuntimeException("이미 확정된 예약은 방문 24시간 이내에 취소할 수 없습니다, 가게로 연락 부탁드립니다.");
        }

        // 미래의 예약이면 예약 가능인원 증가
        if(book.getBookingDate().isAfter(LocalDateTime.now())) {
            availabilityRepository.findByDinerIdAndDateAndTime(book.getDiner().getId(), book.getBookingDate().toLocalDate(), book.getBookingDate().toLocalTime())
                    .ifPresent(availability -> availability.addCapacity(book.getPersonnel()));
        }

        // 예약 삭제
        bookRepository.deleteById(bookId);
        OwnerServiceImpl.CancelManager.allowedBookingIds.remove(bookId);
        OwnerServiceImpl.CancelManager.allowedBookingIds.remove(bookId);

        // 알람 이벤트
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formatterDate = book.getBookingDate().format(dtf);

        eventPublisher.publishEvent(new ReservationCancelEvent(
                book.getDiner().getOwner().getId(),
                book.getDiner().getDinerName(),
                book.getMember().getName(),
                formatterDate
        ));
    }

    @Override
    @Transactional
    public void createBooking(BookDto dto) {
        checkDuplicateBooking(dto.getMemberId(), dto.getBookingDate(), null);
        boolean exists = bookRepository.existsByMember_IdAndBookingDate(dto.getMemberId(), dto.getBookingDate());
        if (exists) {
            throw new IllegalArgumentException("날짜를 확인해주세요. 해당 날짜에 이미 예약이 존재합니다.");
        }

        Diner diner = dinerRepository.findById(dto.getDinerId())
                .orElseThrow(() -> new IllegalArgumentException("식당을 찾을 수 없습니다."));

        // 시간대별 최대 인원 제한을 위해 구현
        LocalDate date = dto.getBookingDate().toLocalDate();
        LocalTime time = dto.getBookingDate().toLocalTime();

        DinerAvailability availability = availabilityRepository.findByDinerIdAndDateAndTime(diner.getId(), date, time).orElseGet(() ->
                availabilityRepository.save(
                        DinerAvailability.builder()
                                .diner(diner)
                                .date(date)
                                .time(time)
                                .maxCapacity(diner.getDefaultMaxCapacity() != null ? diner.getDefaultMaxCapacity() : 10)
                                .currentCapacity(diner.getDefaultMaxCapacity() != null ? diner.getDefaultMaxCapacity() : 10)
                                .build()
                ));

        availability.removeCapacity(dto.getPersonnel());

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

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formatterDate = book.getBookingDate().format(dtf);

        eventPublisher.publishEvent(new ReservationCreateEvent(
                book.getDiner().getOwner().getId(),
                book.getDiner().getDinerName(),
                book.getMember().getName(),
                formatterDate
        ));
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

        if ("unknown_user".equals(book.getMember().getUsername())) {
            throw new IllegalStateException("탈퇴한 회원의 예약은 승인할 수 없습니다.");
        }

        if(Boolean.TRUE.equals(book.getSuccess())) {
            throw new IllegalStateException("이미 승인된 예약입니다.");
        }
        book.setSuccess(true);


        // 알람 이벤트
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formatterDate = book.getBookingDate().format(dtf);

        eventPublisher.publishEvent(new ReservationApproveEvent(
                book.getMember().getId(),     // 알림 받을 유저 ID
                book.getDiner().getDinerName(),     // 식당 이름
                formatterDate      // 예약 시간
        ));
    }

    @Override
    public void rejectBooking(Long bookId) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new IllegalArgumentException("예약이 존재하지 않습니다."));

        // 예약 가능인원 증가
        availabilityRepository.findByDinerIdAndDateAndTime(book.getDiner().getId(), book.getBookingDate().toLocalDate(), book.getBookingDate().toLocalTime())
                .ifPresent(availability -> availability.addCapacity(book.getPersonnel()));

        // 예약 삭제
        bookRepository.delete(book);

        // 알람 이벤트
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formatterDate = book.getBookingDate().format(dtf);

        eventPublisher.publishEvent(new ReservationRejectEvent(
                book.getMember().getId(),
                book.getDiner().getDinerName(),
                formatterDate
        ));
    }

    // 최초 슬롯 생성
    public List<SlotResponseDto> getDailyAvailability(LocalDate date, int requestedPersonnel, Long dinerId) {

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);


        List<Object[]> results = bookRepository.findBookingStatus(dinerId, start, end);
        Map<LocalTime, Integer> bookingMap = new HashMap<>();
        for (Object[] row : results) {
            LocalDateTime dateTime = (LocalDateTime) row[0];
            bookingMap.put(dateTime.toLocalTime(), ((Long) row[1]).intValue());
        }

        int maxCapacity = dinerRepository.findById(dinerId).map(Diner::getDefaultMaxCapacity).orElse(20);

        List<LocalTime> operatingHours = List.of(
                LocalTime.of(11,0), LocalTime.of(11,30), LocalTime.of(12,0), LocalTime.of(12,30),
                LocalTime.of(13,0), LocalTime.of(13,30), LocalTime.of(14,0),
                LocalTime.of(17,0), LocalTime.of(17,30), LocalTime.of(18,0),
                LocalTime.of(18,30), LocalTime.of(19,0), LocalTime.of(19,30), LocalTime.of(20,0)
        );

        return operatingHours.stream().map(time -> {
            int booked = bookingMap.getOrDefault(time, 0);
            int remaining = maxCapacity - booked;

            // 예약 가능 조건: (남은좌석 >= 요청인원) AND (오늘이라면 현재 시간 이후)
            boolean canBook = remaining >= requestedPersonnel;
            if (date.equals(LocalDate.now()) && time.isBefore(LocalTime.now().plusMinutes(10))) {
                canBook = false;
            }

            return new SlotResponseDto(
                    time.format(DateTimeFormatter.ofPattern("HH:mm")),
                    remaining,
                    canBook
            );
        }).collect(Collectors.toList());
    }

    private void checkDuplicateBooking(Long memberId, LocalDateTime requestedTime, Long currentBookId) {

        LocalDateTime start = requestedTime.minusMinutes(60);
        LocalDateTime end = requestedTime.plusMinutes(60);

        boolean hasConflict = bookRepository.existsConflictBooking(memberId, start, end, currentBookId);

        if (hasConflict) {
            throw new IllegalArgumentException("이미 1시간 이내에 다른 예약이 존재합니다.");
        }
    }
}