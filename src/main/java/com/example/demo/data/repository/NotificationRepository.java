package com.example.demo.data.repository;

import com.example.demo.data.enums.NotificationType;
import com.example.demo.data.model.Notification;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByMemberIdOrderByCreatedAtDesc(Long id);

    @Transactional
    void deleteByMemberIdAndRole(Long memberId, String role);

    //사장님이 받는 알림
    List<Notification> findByOwnerIdOrderByCreatedAtDesc(Long id);

    @Transactional
    void deleteByOwnerIdAndRole(Long ownerId, String role);

    List<Notification> findByTypeIn(List<NotificationType> types);
}
