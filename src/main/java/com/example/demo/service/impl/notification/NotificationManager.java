package com.example.demo.service.impl.notification;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationManager {

    // because Multithread environment
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    // SSE 연결을 시도 및 사후처리
    public SseEmitter subscribe(String role, Long id) {
        String subscribeKey = role + "_" + id;
        // Timeout : 1Hours
        SseEmitter emitter = new SseEmitter(60L * 1000 * 60);

        // Remove from map : connection complete or Timeout(error)
        emitter.onCompletion(() -> emitters.remove(subscribeKey));
        emitter.onTimeout(() -> emitters.remove(subscribeKey));
        emitter.onError((e) -> emitters.remove(subscribeKey));

        emitters.put(subscribeKey, emitter);

        try {
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        }
        catch (IOException e) {
            emitters.remove(subscribeKey);
        }

        return emitter;
    }

    public void send(String role, Long id, String message) {
        String subscribeKey = role + "_" + id;
        SseEmitter emitter = emitters.get(subscribeKey);
        if(emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("notification").data(message, MediaType.APPLICATION_JSON));
            }
            catch (IOException e) {
                emitters.remove(subscribeKey);
            }
        }
    }
}
