package com.example.demo.data.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Authority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;            // 권한 id(column)
    private String authority;   // 권한 이름
    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;      // 해당 권한을 가진 member
}
