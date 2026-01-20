package com.example.demo.common.python;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class PythonServerManager {

    private Process pythonProcess;

    @PostConstruct
    public void startPythonServer() {
        Thread serverThread = new Thread(() -> {
            try {
                String projectRoot = System.getProperty("user.dir");
                // 가상환경 내 uvicorn 실행 파일 경로 (Windows 기준)
                String uvicornPath = projectRoot + File.separator + ".venv" + File.separator + "Scripts" + File.separator + "uvicorn.exe";
                // macOS 기준 경로
                // String uvicornPath = projectRoot + File.separator + ".venv" + File.separator + "bin" + File.separator + "uvicorn";
                String appLocation = "main:app";

                ProcessBuilder pb = new ProcessBuilder(
                        uvicornPath,
                        appLocation,
                        "--host", "127.0.0.1",
                        "--port", "8000",
                        "--reload",     // 수정 시 자동 재시작
                        "--no-access-log"
                );

                // 파이썬 코드가 모여있는 디렉토리를 작업 경로로 설정
                pb.directory(new File(projectRoot + File.separator + "python"));
                pb.inheritIO(); // 파이썬 서버 로그를 스프링 콘솔에서 같이 보기 위함

                this.pythonProcess = pb.start();
                System.out.println("🚀 FastAPI 서버가 포트 8000에서 시작되었습니다.");

            } catch (IOException e) {
                System.err.println("❌ FastAPI 서버 시작 실패: " + e.getMessage());
            }
        });

        serverThread.setDaemon(true); // 스프링 종료 시 같이 종료되도록 데몬 설정
        serverThread.start();
    }

    @PreDestroy
    public void stopPythonServer() {
        if (this.pythonProcess != null && this.pythonProcess.isAlive()) {
            this.pythonProcess.destroy();
            System.out.println("🛑 FastAPI 서버가 종료되었습니다.");
        }
    }
}
