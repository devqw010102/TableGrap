package com.example.demo.data.repository;

import com.example.demo.data.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // 마이페이지에서 리뷰 가져오기
    List<Review> findByMemberId(Long MemberId);
    // 식당 리뷰 5개 가져오기
    List<Review> findTop5ByDinerIdOrderByCreateTimeDesc(Long dinerId);
    //리뷰 수정 위해 추가
    Optional<Review> findByBookId(Long bookId);
}
