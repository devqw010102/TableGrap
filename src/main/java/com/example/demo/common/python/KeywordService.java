package com.example.demo.common.python;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeywordService {

    private final PythonProcessExecutor pythonProcessExecutor;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @SuppressWarnings("unchecked")
    public List<String> getKeywordFromAi(String content) {
        try {
            Map<String, String> dataMap = Map.of("content", content);
            String jsonData = objectMapper.writeValueAsString(dataMap);

            String resultJson = pythonProcessExecutor.execute("analyze", "keywords", jsonData, false);

            Map<String, Object> response = objectMapper.readValue(resultJson, Map.class);

            // 여기서 발생하는 타입 체크 경고가 무시됩니다.
            return (List<String>) response.get("keywords");

        } catch (Exception e) {
            log.error("AI 분석 실패: {}", e.getMessage());
            return List.of();
        }
    }
}
