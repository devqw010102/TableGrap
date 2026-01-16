package com.example.demo.service.impl;

import com.example.demo.data.repository.DinerRepository;
import com.example.demo.data.repository.ReviewRepository;
import com.example.demo.service.IndexService;
import lombok.RequiredArgsConstructor;
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

        List<Map<String, Object>> candidates = dinerRepository.findTopRatedDiners();
        if(candidates.isEmpty()) return null;

        Map<String, Object> featured = candidates.getFirst();
        Long dinerId = (Long)featured.get("id");
        String dinerName = (String)featured.get("dinerName");

        List<String> comments = reviewRepository.findCommentsByDinerId(dinerId);

        Map<String, Object> result = new HashMap<>();
        result.put("dinerName", dinerName);
        result.put("comments", comments);

        return result;
    }
}
