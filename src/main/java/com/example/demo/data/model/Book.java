package com.example.demo.data.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

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
    private Date addDate;           // 예약한 시점의 날짜
    private Date reservationDate;   // 실제 예약된 날짜
    private Integer personnel;      // 예약 인원
    private Boolean isSuccess;      // 예약 성공여부
    @ManyToOne
    @JoinColumn(name = "dinerName")
    private Diner dinerName;        // 식당이름
    @ManyToOne
    @JoinColumn(name = "userName")
    private Member name;            // 예약자명
}
