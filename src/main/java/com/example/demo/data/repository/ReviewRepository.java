package com.example.demo.data.repository;

import com.example.demo.data.dto.ReviewDto;
import com.example.demo.data.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
  // 식당 리뷰 5개 가져오기
  List<Review> findTop5ByDinerId(Long dinerId, Long memberId);
}
