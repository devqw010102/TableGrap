package com.example.demo.data.repository;

import com.example.demo.data.model.Owner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OwnerRepository extends JpaRepository<Owner, Long> {
  Optional<Owner> findByEmail(String email);
  Optional<Owner> findByUsername(String username);
  boolean existsByUsername(String username);

}
