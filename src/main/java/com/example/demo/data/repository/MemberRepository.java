package com.example.demo.data.repository;

import com.example.demo.data.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {

    // 이름과 이메일로 사용자 찾기 (아이디 찾기용)
    Optional<Member> findByNameAndEmail(String name, String email);
    // 아이디와 이메일이 동시에 일치하는 회원이 있는지 확인 (본인 인증용)
    boolean existsByUsernameAndEmail(String username, String email);

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
