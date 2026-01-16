package com.example.demo.data.model;

import com.example.demo.data.enums.DinerStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Diner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;            // column 번호
    private String dinerName;   // 식당 이름
    private String category;    // 카테고리
    private String location;    // 위치(~동)
    private String tel;         // 전화번호
    private Double dx;          // x좌표
    private Double dy;          // y좌표
    @Enumerated(EnumType.STRING)
    private DinerStatus status;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = true)
    private Owner owner;
    private String businessNum;  // 사업자 등록번호
    @Builder.Default
    @Column(nullable = false)
    private Integer defaultMaxCapacity = 10;

    // Keyword
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "diner_keywords", joinColumns = @JoinColumn(name = "diner_id"))
    @Column(name = "keyword")
    @Builder.Default
    private List<String> keywords = new ArrayList<>();
}
