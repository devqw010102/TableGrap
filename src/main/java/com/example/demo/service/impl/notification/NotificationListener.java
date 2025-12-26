package com.example.demo.service.impl.notification;

import com.example.demo.data.dto.notification.*;
import com.example.demo.data.model.Book;
import com.example.demo.data.model.Notification;
import com.example.demo.data.repository.BookRepository;
import com.example.demo.data.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final BookRepository bookRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationManager notificationManager;

    @Async
    @EventListener
    @Transactional
    // 식당 폐업 시
    public void handleDinerClosed(DinerClosedEvent event) {
        List<Book> books =  bookRepository.findByDinerId(event.dinerId());

        for(Book book : books){
            String msg = String.format("[%s] 식당이 폐업하여 예약이 자동 취소되었습니다.", event.dinerName());
            sendAndSave("ROLE_USER", book.getMember().getId(), msg);
        }
    }

    @Async
    @EventListener
    // 예약 승인 시
    public void handleReservationApprove(ReservationApproveEvent event) {
        String msg = String.format("[%s] %s 예약이 승인되었습니다!", event.dinerName(), event.reservationTime());
        sendAndSave("ROLE_USER", event.memberId(), msg);
    }

    @Async
    @EventListener
    // 예약 거부 시
    public void handleReservationReject(ReservationRejectEvent event) {
        String msg = String.format("[%s] %s 예약이 거부되었습니다", event.dinerName(), event.reservationTime());
        sendAndSave("ROLE_USER", event.memberId(), msg);
    }

    @Async
    @EventListener
    // 예약 취소 시
    public void handleReservationCancel(ReservationCancelEvent event) {
        String msg = String.format("[%s] %s 고객님의 %s 예약이 취소되었습니다.", event.dinerName(), event.memberName(), event.reservationTime());
        sendAndSave("ROLE_OWNER", event.ownerId(), msg);
    }

    @Async
    @EventListener
    // 리뷰 작성 시
    public void handleReviewWrite(ReviewWriteEvent event) {
        String msg = String.format("[%s] 새로운 리뷰가 작성되었습니다.", event.dinerName());
        sendAndSave("ROLE_OWNER", event.memberId(), msg);
    }

    // Notification Entity Save + Send
    private void sendAndSave(String role, Long receiveId, String message) {
        Notification notification = Notification.builder()
                .memberId(receiveId)
                .role(role)
                .message(message)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
        // 현재 접속 중인 경우(SSE)
        notificationManager.send(role, receiveId, message);
    }
}
