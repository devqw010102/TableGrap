package com.example.demo.data.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookId;            // column 번호
    @CreatedDate
    private LocalDateTime addDate;           // 예약한 시점의 날짜
    private LocalDateTime bookingDate;       // 실제 예약된 날짜
    private Integer personnel;      // 예약 인원
    private Boolean success = false;      // 예약 성공여부
    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "diner_id")
    private Diner diner;        // 식당이름
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;            // 예약자명
}
