package com.example.demo.data.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"diner_id", "date", "time"})
})              // 복합 키 제약조건
public class DinerAvailability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;            // Column 번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diner_id", nullable = false)
    private Diner diner;        // 식당

    @Column(nullable = false)
    private LocalDate date;     // 날짜
    @Column(nullable = false)
    private LocalTime time;     // 시간
    @Column(nullable = false)
    private Integer maxCapacity;   // 최대 인원
    @Column(nullable = false)
    private Integer currentCapacity;   // 남은 인원

    // 예약시 인원차감
    public void removeCapacity(int personCount) {
        int restCapacity = this.currentCapacity - personCount;
        if (restCapacity < 0) {
            throw new IllegalStateException("잔여 좌석이 부족합니다.");
        }
        this.currentCapacity = restCapacity;
    }

    // 예약 취소, 거절 시 인원 증가
    public void addCapacity(int personCount) {
        this.currentCapacity += personCount;
    }
}
