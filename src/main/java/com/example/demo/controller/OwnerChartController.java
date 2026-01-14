package com.example.demo.controller;

import com.example.demo.common.python.PythonProcessExecutor;
import com.example.demo.service.OwnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ownerPage/charts")
public class OwnerChartController {
    private final OwnerService ownerService;
    private final PythonProcessExecutor pythonProcessExecutor;

    @GetMapping("/reviewCharts")
    public String reviewCharts(Long dinerId) {
        try{
            List<Map<String, Object>> avgRate = ownerService.getAvgRate(dinerId);

            ObjectMapper mapper = new ObjectMapper();
            String jsonData = mapper.writeValueAsString(avgRate);

            System.out.println("jsonData: " + jsonData);

            return pythonProcessExecutor.execute("owner", "avgRate_chart", jsonData);
        } catch(Exception e){
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }
}
