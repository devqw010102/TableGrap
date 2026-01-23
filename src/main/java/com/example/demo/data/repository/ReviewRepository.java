package com.example.demo.data.repository;

import com.example.demo.data.dto.ReviewChartByDinerDto;
import com.example.demo.data.dto.ReviewChartDto;
import com.example.demo.data.dto.admin.AdminReviewDto;
import com.example.demo.data.dto.owner.OwnerReviewDto;
import com.example.demo.data.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // 마이페이지에서 리뷰 가져오기
    List<Review> findByMemberId(Long MemberId);

    // 식당 리뷰 5개 가져오기
    List<Review> findTop5ByDinerIdOrderByCreateTimeDesc(Long dinerId);

    //리뷰 수정 위해 추가
    Optional<Review> findByBookId(Long bookId);

    @Query("""
        select new com.example.demo.data.dto.admin.AdminReviewDto(
            r.reviewId,
            m.username,
            d.dinerName,
            r.rating,
            r.comment,
            r.createTime
        )
        from Review r
        join Member m on r.memberId = m.id
        join Diner d on r.dinerId = d.id
    """)
    Page<AdminReviewDto> findAllForAdmin(Pageable pageable);

    @Query("""
    select new com.example.demo.data.dto.admin.AdminReviewDto(
        r.reviewId,
        m.username,
        d.dinerName,
        r.rating,
        r.comment,
        r.createTime
    )
    from Review r
    join Member m on r.memberId = m.id
    join Diner d on r.dinerId = d.id
    where r.createTime >= :start
      and r.createTime < :end
    order by r.createTime desc
""")
    List<AdminReviewDto> findTodayReviews(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );

    // Owner 의 식당 중 카테고리로 선택된 식당의 리뷰
    @Query("""
        select new com.example.demo.data.dto.owner.OwnerReviewDto(
            r.reviewId,
            m.username,
            d.dinerName,
            r.rating,
            r.comment,
            r.createTime
        )
        from Review r
        join Member m on r.memberId = m.id
        join Diner d on r.dinerId = d.id
        where r.dinerId = :dinerId
    """)
    Page<OwnerReviewDto> findReviewByDinerId(@Param("dinerId") Long dinerId, Pageable pageable);

    // Owner 의 식당들 리뷰 전체
    @Query("""
        select new com.example.demo.data.dto.owner.OwnerReviewDto(
            r.reviewId,
            m.username,
            d.dinerName,
            r.rating,
            r.comment,
            r.createTime
        )
        from Review r
        join Member m on r.memberId = m.id
        join Diner d on r.dinerId = d.id
        where r.dinerId in :dinerIds
    """)
    Page<OwnerReviewDto> findReviewByDinerIds(@Param("dinerIds") List<Long> dinerIds, Pageable pageable);

    // delete member for dummy data
    @Modifying
    @Query("UPDATE Review r SET r.memberId= :dummyId WHERE r.memberId = :memberId")
    void updateMemberToDummy(@Param("memberId") Long memberId, @Param("dummyId") Long dummyId);

    // 특정 식당의 모든 리뷰 코멘트만 가져오기
    @Query("SELECT r.comment FROM Review r WHERE r.dinerId = :dinerId")
    List<String> findCommentsByDinerId(@Param("dinerId") Long dinerId);


    // 리뷰 갯수, 평균
    @Query("""
            SELECT new com.example.demo.data.dto.ReviewChartDto
            (r.dinerId, d.dinerName, AVG(r.rating), COUNT(r))
            FROM Review r Join Diner d ON
            r.dinerId = d.id
            WHERE d.owner.id = :ownerId
            GROUP BY r.dinerId, d.dinerName
            """)
    List<ReviewChartDto> findAvgRatingByOwnerId(@Param("ownerId") Long ownerId);

    // 식당별 월간 리뷰 갯수 및 평균, 날짜는 x축에 사용하기 위해 문자열로 받음
    @Query("""
          SELECT new com.example.demo.data.dto.ReviewChartByDinerDto
          (r.dinerId, d.dinerName, CAST(FUNCTION('FORMATDATETIME', r.createTime, 'yyyy-MM') AS string), AVG(r.rating), COUNT(r))
          FROM Review r JOIN Diner d ON r.dinerId = d.id
          WHERE d.owner.id = :ownerId And r.dinerId = :dinerId
          GROUP BY r.dinerId, d.dinerName, CAST(FUNCTION('FORMATDATETIME', r.createTime, 'yyyy-MM') AS string)
          """)
    List<ReviewChartByDinerDto> findAvgByOwnerIdAndDinerId(@Param("dinerId") Long dinerId, @Param("ownerId") Long ownerId);

    // 특정 식당의 모든 리뷰 가져오기
    List<Review> findByDinerId(@Param("dinerId") Long dinerId);

    // 리뷰중 Keyword가 비어있는것만 가져오기(보정 작업)
    List<Review> findByKeywordsIsEmpty();
}