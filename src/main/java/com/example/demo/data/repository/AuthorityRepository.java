package com.example.demo.data.repository;

import com.example.demo.data.model.Authority;
import com.example.demo.data.model.Member;
import com.example.demo.data.model.Owner;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AuthorityRepository extends JpaRepository<Authority, Long>{
    List<Authority> findByMember(Member member);
    List<Authority> findByOwner(Owner owner);

    @Query(value = """
    SELECT 'ROLE_USER' AS authority, COUNT(DISTINCT m.id) AS count 
    FROM member m
    JOIN authority a ON m.id = a.member_id
    WHERE a.authority != 'ROLE_ADMIN'
    UNION ALL
    SELECT 'ROLE_OWNER' AS authority, COUNT(DISTINCT o.id) AS count 
    FROM owner o
    """, nativeQuery = true)
    List<Object[]> countByRoleForDashboard();

    @Modifying
    @Transactional
    void deleteByMember_Id(Long memberId);
}
