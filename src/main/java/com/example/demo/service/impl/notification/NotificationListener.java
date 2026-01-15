package com.example.demo.service.impl.notification;

import com.example.demo.data.dto.notification.*;
import com.example.demo.data.enums.AuthorityStatus;
import com.example.demo.data.enums.NotificationType;
import com.example.demo.data.model.Notification;
import com.example.demo.data.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationRepository notificationRepository;
    private final NotificationManager notificationManager;

    @Async
    @EventListener
    // 예약 승인 시 (사장 응답 시간 계산을 위해 ownerId가 필요함)
    public void handleReservationApprove(ReservationApproveEvent event) {
        String msg = String.format("[%s] %s 예약이 승인되었습니다!", event.dinerName(), event.reservationTime());
        // 3번째 인자에 event.ownerId() 전달
        sendAndSave(AuthorityStatus.ROLE_USER.getCode(), event.memberId(), event.ownerId(), msg, NotificationType.RESERVATION_APPROVE);
    }

    @Async
    @EventListener
    // 예약 거부 시
    public void handleReservationReject(ReservationRejectEvent event) {
        String msg = String.format("[%s] %s 예약이 거부되었습니다", event.dinerName(), event.reservationTime());
        // 3번째 인자에 null 전달 (필요시 RejectEvent도 ownerId 추가하여 개선 가능)
        sendAndSave(AuthorityStatus.ROLE_USER.getCode(), event.memberId(), null, msg, NotificationType.RESERVATION_REJECT);
    }

    @Async
    @EventListener
    // 예약 취소 시
    public void handleReservationCancel(ReservationCancelEvent event) {
        String msg = String.format("[%s] %s 고객님의 %s 예약이 취소되었습니다.", event.dinerName(), event.memberName(), event.reservationTime());
        sendAndSave(AuthorityStatus.ROLE_OWNER.getCode(), event.ownerId(), null, msg, NotificationType.RESERVATION_CANCEL);
    }

    @Async
    @EventListener
    // 리뷰 작성 시
    public void handleReviewWrite(ReviewWriteEvent event) {
        String msg = String.format("[%s] 새로운 리뷰가 작성되었습니다.", event.dinerName());
        sendAndSave(AuthorityStatus.ROLE_OWNER.getCode(), event.ownerId(), null, msg, NotificationType.REVIEW_WRITE);
    }

    @Async
    @EventListener
    // 회원가입 성공 시(유저, 사장)
    public void handleRegisterUser(RegisterEvent event) {
        String msg = String.format("[%s] 회원가입을 환영합니다.", event.name());
        sendAndSave(event.role(), event.memberId(), null, msg, NotificationType.NONE);
    }

    @Async
    @EventListener
    // 회원 수정 시(유저)
    public void handleMemberUpdate(MemberUpdateEvent event) {
        String msg = String.format("[%s] 정보가 수정되었습니다.", event.name());
        sendAndSave(AuthorityStatus.ROLE_USER.getCode(), event.memberId(), null, msg, NotificationType.NONE);
    }

    @Async
    @EventListener
    // 회원 수정 시(사장)
    public void handleOwnerUpdate(OwnerUpdateEvent event) {
        String msg = String.format("[%s] 정보가 수정되었습니다.", event.name());
        sendAndSave(AuthorityStatus.ROLE_OWNER.getCode(), event.ownerId(), null, msg, NotificationType.NONE);
    }

    @Async
    @EventListener
    // 새로운 예약이 들어왔을 때
    public void handleReservationCreate(ReservationCreateEvent event) {
        String msg = String.format("[%s] %s 예약이 신청되었습니다.", event.dinerName(), event.reservationTime());
        sendAndSave(AuthorityStatus.ROLE_OWNER.getCode(), event.ownerId(), null, msg, NotificationType.RESERVATION_CREATE);
    }

    @Async
    @EventListener
    // 예약 수정 시
    public void handleReservationUpdate(ReservationUpdateEvent event) {
        String msg = String.format("[%s] %s 예약이 수정되었습니다.", event.dinerName(), event.reservationTime());
        sendAndSave(AuthorityStatus.ROLE_OWNER.getCode(), event.ownerId(), null, msg, NotificationType.RESERVATION_UPDATE);
    }

    @Async
    @EventListener
    // 예약 취소 요청 시
    public void handleReservationCancelRequest(ReservationCancelRequestEvent event) {
        String msg = String.format("[%s] %s 식당 사정으로 예약 취소 요청이 왔습니다.", event.dinerName(), event.reservationTime());
        sendAndSave(AuthorityStatus.ROLE_USER.getCode(), event.memberId(), null, msg, NotificationType.RESERVATION_CANCEL_REQUEST);
    }

    // Notification Entity Save + Send (5개 파라미터로 고정)
    private void sendAndSave(String role, Long receiveId, Long explicitOwnerId, String message, NotificationType type) {
        boolean isOwner = AuthorityStatus.ROLE_OWNER.getCode().equals(role);

        Notification notification = Notification.builder()
                .memberId(isOwner ? null : receiveId)
                // explicitOwnerId가 있으면 쓰고, 없으면 기존 방식(사장 수신이면 receiveId) 사용
                .ownerId(explicitOwnerId != null ? explicitOwnerId : (isOwner ? receiveId : null))
                .role(role)
                .message(message)
                .isRead(false)
                .type(type)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
        notificationManager.send(role, receiveId, message);
    }
}