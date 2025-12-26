package com.example.demo.data.repository;

import com.example.demo.data.model.Owner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface OwnerRepository extends JpaRepository<Owner, Long> {
  Optional<Owner> findByEmail(String email);
  Optional<Owner> findByUsername(String username);
  boolean existsByUsername(String username);
  Optional<Owner> findById(Long id);

  @Query("""
   SELECT count(DISTINCT o)
   FROM Owner o
   WHERE NOT EXISTS (
     SELECT 1 FROM Authority a
     WHERE a.owner = o
       AND a.authority = 'ROLE_ADMIN'
     )
  """)
  long countOwnersExceptAdmin();
}
