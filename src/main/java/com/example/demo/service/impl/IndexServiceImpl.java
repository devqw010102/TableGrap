package com.example.demo.service.impl;

import com.example.demo.data.model.Diner;
import com.example.demo.data.repository.DinerRepository;
import com.example.demo.data.repository.ReviewRepository;
import com.example.demo.service.IndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class IndexServiceImpl implements IndexService {

    private final DinerRepository dinerRepository;
    private final ReviewRepository reviewRepository;

    @Override
    public List<Map<String, Object>> getCategoryRankingStats() {
        return dinerRepository.findCategoryStats();
    }

    @Override
    public Map<String, Object> getFeaturedDinerData() {
        // 리뷰 2개 이상 & (리뷰수 + 최신작성일) 가중치로 식당 선정
        Pageable topOne = PageRequest.of(0, 1);
        List<Diner> diners = dinerRepository.findFeaturedDiners(topOne);

        if (diners.isEmpty()) return null;

        Diner featuredDiner = diners.get(0);
        Long dinerId = featuredDiner.getId();

        // 2. Python의 kiwi가 분석할 수 있도록 해당 식당의 모든 댓글 조회
        List<String> comments = reviewRepository.findCommentsByDinerId(dinerId);

        Map<String, Object> result = new HashMap<>();
        result.put("dinerName", featuredDiner.getDinerName());
        result.put("comments", comments); // Python으로 통째로 전달

        return result;
    }
}
