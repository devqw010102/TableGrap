package com.example.demo.data.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;
    private Long memberId;
    private Long bookId;
    private Long dinerId;
    private int rating;
    private String comment;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createTime;
    @LastModifiedDate
    private LocalDateTime updateTime;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "review_keywords", // 생성될 테이블 이름
            joinColumns = @JoinColumn(name = "review_id") // 외래키 컬럼명
    )
    @Column(name = "keyword") // 키워드 값이 저장될 컬럼명
    @Builder.Default // 빌더 패턴 사용 시 초기화 보장
    private List<String> keywords = new ArrayList<>();
}
