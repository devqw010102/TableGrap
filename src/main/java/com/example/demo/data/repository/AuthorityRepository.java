package com.example.demo.data.repository;

import com.example.demo.data.model.Authority;
import com.example.demo.data.model.Member;
import com.example.demo.data.model.Owner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AuthorityRepository extends JpaRepository<Authority, Long>{
    List<Authority> findByMember(Member member);
    List<Authority> findByOwner(Owner owner);
    boolean existsByMemberAndAuthority(Owner owner, String authority);

    @Query("""
    select a.authority, count(distinct a.member.id)
    from Authority a
    where a.authority != 'ROLE_ADMIN'
    group by a.authority
""")
    List<Object[]> countByAuthority();
}
