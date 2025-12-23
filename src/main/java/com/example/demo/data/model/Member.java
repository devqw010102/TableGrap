package com.example.demo.data.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;        // column 번호

    @Column(unique = true, nullable = false)
    private String username;    // 아이디

    @Column(nullable = false)
    private String password;    // 비밀번호

    @Column(unique = true, nullable = false)
    private String email;       // 이메일

    private String phone;       // 전화번호
    @Column(nullable = false)

    private String name;        // 이름
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude

    private List<Authority> authorities = new ArrayList<>();
}
