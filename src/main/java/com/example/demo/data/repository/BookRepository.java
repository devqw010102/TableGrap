package com.example.demo.data.repository;

import com.example.demo.data.dto.owner.BookOwnerResponseDto;
import com.example.demo.data.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookRepository extends JpaRepository<Book,Long> {

    List<Book> findByMember_id(Long memberId);

    @Query("""
    select new com.example.demo.data.dto.owner.BookOwnerResponseDto(
        b.bookId,
        d.dinerName,
        b.bookingDate,
        b.personnel,
        m.name
    )
    from Book b
    join b.diner d
    join b.member m
    where d.owner.id = :ownerId
      and (:dinerId is null or d.id = :dinerId)
      and (
            :pending is null
            or (:pending = true and (b.success = false or b.success is null))
            or (:pending = false and b.success = true)
          )
      and (:start is null or b.bookingDate >= :start)
      and (:end is null or b.bookingDate < :end)
    order by b.bookingDate asc, b.personnel asc
""")
    Page<BookOwnerResponseDto> findBookings(
            @Param("ownerId") Long ownerId,
            @Param("dinerId") Long dinerId,
            @Param("pending") Boolean pending,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );

    @Query("""
    select count(b)
    from Book b
    where b.addDate >= :start
      and b.addDate < :end
""")
    long countTodayBookings(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    boolean existsByMember_IdAndBookingDate(Long memberId, LocalDateTime bookingDate);

    // Notification
    List<Book> findByDinerId(Long dinerId);

    //식당 삭제전 해당 식당의 예약 존재 확인
    boolean existsByDiner_IdAndBookingDateAfter(Long dinerId, LocalDateTime now);

    // 특정 식당의 모든 예약 삭제
    void deleteAllByDiner_Id(Long dinerId);

    @Query("SELECT b.bookingDate, SUM(b.personnel) " +
            "FROM Book b " +
            "WHERE b.diner.id = :dinerId " +
            "AND b.bookingDate >= :start " +
            "AND b.bookingDate <= :end " +
            "GROUP BY b.bookingDate")
    List<Object[]> findBookingStatus(@Param("dinerId") Long dinerId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end
    );

    @Query("SELECT COUNT(b) > 0 FROM Book b WHERE b.member.id = :memberId AND (b.success = false OR b.success IS NULL)")
    boolean existsPendingBookings(@Param("memberId") Long memberId);

    boolean existsByMember_IdAndSuccessAndBookingDateAfter(Long memberId, Boolean success, LocalDateTime now);

    // delete member for dummy data
    @Modifying
    @Query("UPDATE Book b SET b.member.id= :dummyId WHERE b.member.id = :memberId")
    void updateMemberToDummy(@Param("memberId") Long memberId, @Param("dummyId") Long dummyId);

    @Query("""
        SELECT COUNT(b) > 0 
            FROM Book b
                WHERE b.member.id = :memberId
                    AND b.bookingDate > :start
                        AND b.bookingDate < :end
                            AND (:excludeBookId IS NULL OR b.bookId <> :excludeBookId)
    """)

    boolean existsConflictBooking(
            @Param("memberId") Long memberId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("excludeBookId") Long excludeBookId

    );

}
