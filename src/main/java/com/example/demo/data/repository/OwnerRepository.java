package com.example.demo.data.repository;

import com.example.demo.data.enums.AccountStatus;
import com.example.demo.data.model.Owner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OwnerRepository extends JpaRepository<Owner, Long> {

    // 아이디 찾기, 비번 재설정 용
    Optional<Owner> findByNameAndEmailAndStatus(String name, String email, AccountStatus status);
    boolean existsByUsernameAndEmailAndStatus(String username, String email, AccountStatus status);

    Optional<Owner> findByEmailAndStatus(String email, AccountStatus status);
    Optional<Owner> findByUsername(String username);
    boolean existsByUsername(String username, AccountStatus status);
    Optional<Owner> findByIdAndStatus(Long id, AccountStatus status);

    @Query("""
        SELECT count(DISTINCT o)
        FROM Owner o
        WHERE NOT EXISTS (
            SELECT 1 FROM Authority a
            WHERE a.owner = o
            AND a.authority IN (:adminRole, :deletedRole)
            )
    """)
    long countOwnersExceptAdminAndDeleted(
            @Param("adminRole") String adminRole,
            @Param("deletedRole") String deletedRole
    );

    // 차트 생성을 위한 리뷰 개수 가져오기
    List<Map<String, Object>> getReviewCount(Long dinerId);

    // 식당 별 평점 평균 가져오기
    List<Map<String, Object>> getAvgRate(Long dinerId);
}
