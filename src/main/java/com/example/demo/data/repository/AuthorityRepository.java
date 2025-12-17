package com.example.demo.data.repository;

import com.example.demo.data.model.Authority;
import com.example.demo.data.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthorityRepository extends JpaRepository<Authority, Long>{
    List<Authority> findByMember(Member member);

    boolean existsByMemberAndAuthority(Member member, String authority);
}
