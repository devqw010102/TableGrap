package com.example.demo.controller;

import com.example.demo.common.python.PythonProcessExecutor;
import com.example.demo.data.enums.NotificationType;
import com.example.demo.data.model.Notification;
import com.example.demo.data.repository.NotificationRepository;
import com.example.demo.service.BookService;
import com.example.demo.service.impl.BookServiceImpl;
import com.example.demo.service.impl.DinerServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservation/charts")
public class BookChartController {

    private final PythonProcessExecutor pythonProcessExecutor;
    private final DinerServiceImpl dinerserviceImpl;
    private final NotificationRepository notificationRepository;
    private final ObjectMapper mapper;
    private final BookService bookService;

    // 1. @RequiredArgsConstructor를 지우고 직접 생성자를 만듭니다.
    public BookChartController(PythonProcessExecutor pythonProcessExecutor,
                               BookService bookService,
                               DinerServiceImpl dinerserviceImpl,
                               NotificationRepository notificationRepository) {
        this.pythonProcessExecutor = pythonProcessExecutor;
        this.dinerserviceImpl = dinerserviceImpl;
        this.bookService = bookService;
        this.notificationRepository = notificationRepository;

        // 2. 생성 시점에 ObjectMapper를 설정 (날짜 에러 방지)
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @GetMapping("/owner-response/{ownerId}")
    public String getOwnerResponseChart(@PathVariable Long ownerId) {
        try {
            List<NotificationType> targets = Arrays.asList(
                    NotificationType.RESERVATION_CREATE,
                    NotificationType.RESERVATION_APPROVE,
                    NotificationType.RESERVATION_REJECT);
            List<Notification> rawData = notificationRepository.findByOwnerIdAndTypeIn(ownerId, targets);

            Map<String, Object> payload = new HashMap<>();
            payload.put("ownerId", ownerId);
            payload.put("notification", rawData);

            String jsonData = mapper.writeValueAsString(payload);
            System.out.println("### 파이썬으로 보내는 데이터: " + jsonData);

            return pythonProcessExecutor.execute("reservation", "response_time_analysis", jsonData);
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage().replace("\"", "'") + "\"}";
        }
    }

    @GetMapping("/visitor-trend/{dinerId}")
    public String getVisitorTrendChart(@PathVariable Long dinerId) {
        try {
            List<Map<String, Object>> rawData = bookService.getVisitorData(dinerId);

            Map<String, Object> payload = new HashMap<>();
            payload.put("dinerId", dinerId);
            payload.put("visitor", rawData);

            String jsonData = mapper.writeValueAsString(payload);
            System.out.println("### 파이썬으로 보내는 방문 추이 데이터:" + jsonData);

            return pythonProcessExecutor.execute("reservation", "visitorBarchart", jsonData);

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"" + e.getMessage().replace("\"", "'") + "\"}";
        }

        }
}