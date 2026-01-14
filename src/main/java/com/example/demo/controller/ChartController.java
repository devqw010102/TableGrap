package com.example.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Base64; // 상단에 추가
@RestController
@RequiredArgsConstructor // 이걸 붙이면 objectMapper가 자동으로 주입됩니다.
public class ChartController {

    @GetMapping("/api/test-chart")
    public String getTestChart() {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "E:/KBK/SpringTask/TableGrap/.venv/Scripts/python.exe",
                    "E:/KBK/SpringTask/TableGrap/python/my_chart.py"
            );
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            return reader.lines().collect(Collectors.joining());
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }
}
