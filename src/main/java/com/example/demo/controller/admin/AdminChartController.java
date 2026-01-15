package com.example.demo.controller.admin;

import com.example.demo.common.python.PythonProcessExecutor;
import com.example.demo.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/adminPage/charts")
public class AdminChartController {

    private final PythonProcessExecutor pythonProcessExecutor;
    private final AdminService adminService;

    // 카테고리 차트
    @GetMapping(value = "/diner-categories", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getDinerCategoryChart() {
        try {
            List<Map<String, Object>> stats = adminService.getCategoryStats();
            String jsonData = new ObjectMapper().writeValueAsString(stats);

            // Python 호출 시 한글 깨짐 방지를 위해 true(Base64) 추천
            String result = pythonProcessExecutor.execute("admin", "category_donut_chart", jsonData, true);
            return ResponseEntity.ok(result);
        } catch(Exception e) {
            return ResponseEntity.status(500).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // 최근 1주일 예약 차트
    @GetMapping("/weekly-reservation")
    public String getWeeklyReservationChart() {
        try {
            List<Map<String, Object>> stats = adminService.getWeeklyReservationChart();

            ObjectMapper mapper = new ObjectMapper();
            String jsonData = mapper.writeValueAsString(stats);

            System.out.println("jsonData : " + jsonData);

            return pythonProcessExecutor.execute("admin", "reservation_stats_chart", jsonData);
        }
        catch(Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    // 회원 분포 차트 (가로형 막대)
    @GetMapping("/member-count")
    public String getMemberDistributionChart() {
        try {
            List<Map<String, Object>> stats = adminService.getMemberStatsForChart();

            ObjectMapper  mapper = new ObjectMapper();
            String jsonData = mapper.writeValueAsString(stats);

            System.out.println("jsonData : " + jsonData);

            return pythonProcessExecutor.execute("admin", "member_stats_chart", jsonData);
        }
        catch(Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

}
