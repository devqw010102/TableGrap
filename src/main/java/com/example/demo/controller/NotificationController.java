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
    //권한에 따른 분기 구분
    @GetMapping("/{id}")
    public ResponseEntity<List<Notification>> getNotifications(@PathVariable Long id, @RequestParam String role) {
       List <Notification> lists;
        if("ROLE_OWNER".equals(role)) {
          lists = notificationRepository.findByOwnerIdOrderByCreatedAtDesc(id);
        } else {
            lists = notificationRepository.findByMemberIdOrderByCreatedAtDesc(id);
        }
        return ResponseEntity.ok(lists);
    }
    //검증 로직 분기
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> readNotification(@PathVariable Long id, @RequestParam(required = false) Long memberId,
                                                 @RequestParam(required = false) Long ownerId, @RequestParam String role) {
        Notification notification = notificationRepository.findById(id).orElseThrow();
        boolean isAuthorized=false;
        if("ROLE_OWNER".equals(role)) {
            //owner면 owner db와 구분
            isAuthorized = notification.getOwnerId() != null && notification.getOwnerId().equals(ownerId);
        } else {
            //user면 memeber db와 구분
            isAuthorized = notification.getMemberId() != null && notification.getMemberId().equals(memberId);
        }
        if (!isAuthorized || !notification.getRole().equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        notification.setRead(true);
        notificationRepository.save(notification);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/all/{id}")
    public ResponseEntity<Void> deleteAllNotifications(@PathVariable Long id, @RequestParam String role) {
        if("ROLE_OWNER".equals(role)) {
            notificationRepository.deleteByOwnerIdAndRole(id, role);
        } else {
            notificationRepository.deleteByMemberIdAndRole(id, role);
        }
        return ResponseEntity.ok().build();
    }
}
