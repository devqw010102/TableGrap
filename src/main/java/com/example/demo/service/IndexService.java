package com.example.demo.service;

import java.util.List;
import java.util.Map;

public interface IndexService {
    // 인기 카테고리 차트
    List<Map<String, Object>> getCategoryRankingStats();

    // 오늘의 식당 리뷰 키워드 차트
    Map<String, Object> getFeaturedDinerData();
}
