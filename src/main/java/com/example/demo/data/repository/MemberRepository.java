package com.example.demo.data.repository;

import com.example.demo.data.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {

    Optional<Member> findByEmail(String email);
    boolean existsByUsername(String username);
    Optional<Member> findByUsername(String username);

    @Query("""
    select count(distinct m)
    from Member m
    where not exists (
        select 1 from Authority a
        where a.member = m
          and a.authority = 'ROLE_ADMIN'
    )
""")
    long countMembersExceptAdmin();
}
