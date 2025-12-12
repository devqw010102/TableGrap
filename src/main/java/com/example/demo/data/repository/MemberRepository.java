package com.example.demo.data.repository;

import com.example.demo.data.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {

    Optional<Member> findByEmail(String email);
    boolean existsByUserName(String userName);
}
