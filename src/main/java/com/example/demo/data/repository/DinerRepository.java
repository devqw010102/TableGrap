package com.example.demo.data.repository;

import com.example.demo.data.model.Diner;
import com.example.demo.data.model.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DinerRepository extends JpaRepository<Diner, Long> {
    Page<Diner> findByCategory(Pageable pageable, String category);
    Optional<Diner> findById(Long id);

}
