package com.example.demo.data.model;

import jakarta.persistence.*;
import lombok.*;

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
    @ToString.Exclude
    @JoinColumn(name = "member_id")
    private Member member;      // 해당 권한을 가진 member
    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "owner_id")
    private Owner owner;
}
