package com.example.demo.controller;

import com.example.demo.data.model.Notification;
import com.example.demo.data.repository.NotificationRepository;
import com.example.demo.service.impl.notification.NotificationManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationManager notificationManager;
    private final NotificationRepository notificationRepository;

    @GetMapping(value = "/subscribe/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public SseEmitter subscribe(@PathVariable Long id, @RequestParam String role) {
        return notificationManager.subscribe(role, id);
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<List<Notification>> getNotifications(@PathVariable Long memberId) {
        List<Notification> lists = notificationRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
        return ResponseEntity.ok(lists);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> readNotification(@PathVariable Long id, @RequestParam Long memberId, @RequestParam String role) {
        Notification notification = notificationRepository.findById(id).orElseThrow();

        if (!notification.getMemberId().equals(memberId) || !notification.getRole().equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        notification.setRead(true);
        notificationRepository.save(notification);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/all/{memberId}")
    public ResponseEntity<Void> deleteAllNotifications(@PathVariable Long memberId, @RequestParam String role) {
        notificationRepository.deleteByMemberIdAndRole(memberId, role);
        return ResponseEntity.ok().build();
    }
}
