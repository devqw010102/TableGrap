package com.example.demo.controller.admin;

import com.example.demo.common.python.PythonProcessExecutor;
import com.example.demo.service.AdminService;
import lombok.RequiredArgsConstructor;
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
    @GetMapping("/diner-categories")
    public String getDinerCategoryChart() {
        try {
            // 1. DB 값 불러오기
            List<Map<String, Object>> stats = adminService.getCategoryStats();

            // 2. Mapper 사용하여 Json 으로 변환
            ObjectMapper mapper = new ObjectMapper();
            String jsonData = mapper.writeValueAsString(stats);

            System.out.println("jsonData: " + jsonData);
            // 3. 파이썬 실행
            return pythonProcessExecutor.execute("admin", "category_donut_chart", jsonData);
        }
        catch(Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
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
