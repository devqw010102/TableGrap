package com.example.demo.common.python;

import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Component
public class PythonProcessExecutor {

    public String execute(String domain, String fileName, String jsonData) {
        try {
            // 1. 프로젝트 경로
            String projectRoot = System.getProperty("user.dir");

            // 2. 가상환경 파이썬 경로
            String pythonPath = projectRoot + File.separator + ".venv" + File.separator + "Scripts" + File.separator + "python.exe";

            // 3. 실행할 파이썬 스크립트 경로
            String scriptPath = projectRoot + File.separator + "python" + File.separator + domain + File.separator + fileName + ".py";

            ProcessBuilder pb = new ProcessBuilder(pythonPath, scriptPath);

            pb.environment().put("PYTHONIOENCODING", "UTF-8");
            pb.environment().put("PYTHONPATH", projectRoot + File.separator + ".venv" + File.separator + "Lib" + File.separator + "site-packages");
            Process process = pb.start();

            // 4. 파이썬으로 데이터 전송
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8))) {
                writer.write(jsonData);
                writer.flush();
            }

            // 5. 파이썬으로부터 차트 JSON 읽기
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String result = reader.lines().collect(Collectors.joining());

                if (result.isEmpty()) {
                    readErrorStream(process);
                }

                return result;
            }
        }
        catch(Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    private void readErrorStream(Process process) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
            String errorLog = reader.lines().collect(Collectors.joining());
            if (!errorLog.isEmpty()) {
                System.err.println("Python Error: " + errorLog);
            }
        }
    }
}
