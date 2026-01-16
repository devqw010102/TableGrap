package com.example.demo.controller.index;

import com.example.demo.common.python.PythonProcessExecutor;
import com.example.demo.service.IndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/index/charts")
public class IndexChartController {

    private final PythonProcessExecutor pythonProcessExecutor;
    private final IndexService indexService;


    @GetMapping("/category-ranking")
    public String getIndexCategoryRankingChart() {
        try {
            List<Map<String, Object>> stats = indexService.getCategoryRankingStats();

            ObjectMapper mapper = new ObjectMapper();
            String jsonData = mapper.writeValueAsString(stats);

            System.out.println("jsonData : " + jsonData);

            return pythonProcessExecutor.execute("index", "category_pie_chart", jsonData);
        }
         catch(Exception e) {
             return "{\"error\":\"" + e.getMessage() + "\"}";
         }
    }

    @GetMapping(value = "/best-ratings", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public String getBestRatingChart() {
        try {
            List<Map<String, Object>> stats = indexService.getTop5RatedDiners();

            ObjectMapper mapper = new ObjectMapper();
            String jsonData = mapper.writeValueAsString(stats);

            System.out.println("best-ratings Data : " + jsonData);

            return pythonProcessExecutor.execute("index", "best_rating_chart", jsonData);
        }
        catch(Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    @GetMapping("/featured-keywords")
    public String getFeaturedKeywordsChart() {
        try {
            Map<String, Object> data = indexService.getFeaturedDinerData();

            if(data == null || data.isEmpty()) return "{\"error\":\"데이터가 부족합니다.\"}";

            ObjectMapper mapper = new ObjectMapper();
            String jsonData = mapper.writeValueAsString(data);

            byte[] utf8Bytes = jsonData.getBytes(StandardCharsets.UTF_8);
            String fixedJsonData = new String(utf8Bytes, StandardCharsets.UTF_8);

            System.out.println("최종 검증 JSON: " + fixedJsonData);

            return pythonProcessExecutor.execute("index", "featured_keywords_chart", jsonData, true);
        }
        catch(Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }
}
