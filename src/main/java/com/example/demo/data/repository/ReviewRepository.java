package com.example.demo.data.repository;

import com.example.demo.data.dto.admin.AdminReviewDto;
import com.example.demo.data.dto.owner.OwnerReviewDto;
import com.example.demo.data.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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


    List<Review> findTop5ByDinerIdAndMemberId(Long dinerId, Long memberId);

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
    List<AdminReviewDto> findAllForAdmin();

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
            @Param("end") LocalDateTime end
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

}