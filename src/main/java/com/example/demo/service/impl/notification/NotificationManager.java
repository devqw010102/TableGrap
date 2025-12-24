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
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    // SSE 연결을 시도 및 사후처리
    public SseEmitter subscribe(Long memberId) {
        // Timeout : 1Hours
        SseEmitter emitter = new SseEmitter(60L * 1000 * 60);

        // Remove from map : connection complete or Timeout(error)
        emitter.onCompletion(() -> emitters.remove(memberId));
        emitter.onTimeout(() -> emitters.remove(memberId));
        emitter.onError((e) -> emitters.remove(memberId));

        emitters.put(memberId, emitter);

        try {
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        }
        catch (IOException e) {
            emitters.remove(memberId);
        }

        return emitter;
    }

    public void send(Long memberId, String message) {
        SseEmitter emitter = emitters.get(memberId);
        if(emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("notification").data(message, MediaType.APPLICATION_JSON));
            }
            catch (IOException e) {
                emitters.remove(memberId);
            }
        }
    }
}
