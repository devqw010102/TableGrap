package com.example.demo.common.python;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class PythonProcessExecutor {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String FASTAPI_URL = "http://127.0.0.1:8000";

    // 기본 호출 (Base64 미사용)
    public String execute(String domain, String fileName, String jsonData) {
        return execute(domain, fileName, jsonData, false);
    }

    // FastAPI 엔드포인트 호출 방식
    public String execute(String domain, String fileName, String jsonData, boolean useBase64) {
        try {
            // 1. 엔드포인트 구성 (예: /index/featured_keywords_chart)
            String url = FASTAPI_URL + "/" + domain + "/" + fileName;

            // 2. 데이터 준비
            String sendData = jsonData;
            if (useBase64) {
                sendData = Base64.getEncoder().encodeToString(jsonData.getBytes(StandardCharsets.UTF_8));
            }

            // 3. HTTP Header 및 Body 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("data", sendData);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            // 4. FastAPI 서버로 POST 요청
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                return "{\"error\":\"FastAPI Server Error: " + response.getStatusCode() + "\"}";
            }

        } catch (Exception e) {
            System.err.println("FastAPI 통신 실패: " + e.getMessage());
            return "{\"error\":\"Python API Connection Failed: " + e.getMessage() + "\"}";
        }
    }
}