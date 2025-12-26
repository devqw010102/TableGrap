package com.example.demo.data.repository;

import com.example.demo.data.model.Notification;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByMemberIdOrderByCreatedAtDesc(Long id);

    long countByMemberIdAndIsReadFalse(Long memberId);

    @Transactional
    void deleteByMemberIdAndRole(Long memberId, String role);
}
