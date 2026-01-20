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
import java.util.Map;

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
    //식당 삭제전 해당 식당의 예약 존재 확인
    boolean existsByDiner_IdAndBookingDateAfter(Long dinerId, LocalDateTime now);

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

    // 최근 1주일
    @Query("SELECT " +
            "  FUNCTION('FORMATDATETIME', b.bookingDate, 'yyyy-MM-dd') as date, " +
            "  COUNT(b.bookId) as count " +
            "FROM Book b " +
            "WHERE b.bookingDate >= :startDate " +
            "GROUP BY date " +
            "ORDER BY date ASC")
    List<Map<String, Object>> getWeeklyBookingStats(@Param("startDate") LocalDateTime startDate);

    // 재방문율 계산을 위한 예약 내역 불러오기
    @Query("SELECT b, d.dinerName FROM Book b " +
            "JOIN FETCH b.diner d " +
            "WHERE d.owner.id = :ownerId")
    List<Book> findBookByOwnerId(Long ownerId);

    // 개별 식당 재방문율 계산
    @Query("SELECT b FROM Book b " +
            "JOIN FETCH b.diner d " +
            "WHERE d.id = :dinerId " +
            "AND d.owner.id  = :ownerId " +
            "ORDER BY b.bookingDate ASC")
    List<Book> findBookByDinerIdAndOwnerId(Long dinerId, Long ownerId);

    // visitor trend chart ( 요일/ 시간)
    @Query("SELECT b.bookingDate as date, b.personnel as personnel " +
            "FROM Book b " +
            "WHERE b.diner.id = :dinerId " +
            "AND b.success = true " +
            "AND b.bookingDate < :now " +
            "ORDER BY b.bookingDate ASC")
    List<Map<String, Object>> findVisitorTrendDataForChart(@Param("dinerId") Long dinerId, @Param("now") LocalDateTime now);


    // mypage - category dounut chart
    @Query("""
                SELECT b.diner.category AS category, COUNT(b) AS count 
                FROM Book b 
                WHERE b.member.id = :memberId 
                  AND b.bookingDate < :now 
                GROUP BY b.diner.category
            """)
    List<Map<String, Object>> findFoodPreferencesForChart(
            @Param("memberId") Long memberId,
            @Param("now") LocalDateTime now
    );


    // mypage - montly chart
    // 내 월별 방문 횟수 (최근 6개월, 확정된 지난 예약)
    @Query("""
                SELECT FUNCTION('FORMATDATETIME', b.bookingDate, 'yyyy-MM') AS month, COUNT(b) AS count 
                FROM Book b 
                WHERE b.member.id = :memberId 
                  AND b.success = true 
                  AND b.bookingDate >= :sixMonthsAgo 
                  AND b.bookingDate < :now 
                GROUP BY month 
                ORDER BY month ASC
            """)
    List<Map<String, Object>> findMyMonthlyVisitCount(@Param("memberId") Long memberId, @Param("sixMonthsAgo") LocalDateTime sixMonthsAgo, @Param("now") LocalDateTime now);

    // 전체 사용자의 월평균 방문 횟수 (비교용)
    @Query("""
                SELECT COUNT(b) * 1.0 / (SELECT COUNT(distinct m.id) FROM Member m) 
                FROM Book b 
                WHERE b.success = true 
                  AND b.bookingDate >= :sixMonthsAgo 
                  AND b.bookingDate < :now
            """)
    Double getAverageVisitCountAllUsers(@Param("sixMonthsAgo") LocalDateTime sixMonthsAgo, @Param("now") LocalDateTime now);

    // 나의 상위 백분위 계산을 위한 전체 예약수
    @Query("""
                SELECT COUNT(b) FROM Book b 
                WHERE b.success = true AND b.bookingDate >= :sixMonthsAgo AND b.bookingDate < :now
                GROUP BY b.member.id
            """)
    List<Long> getAllUsersTotalVisits(@Param("sixMonthsAgo") LocalDateTime sixMonthsAgo, @Param("now") LocalDateTime now);

}

