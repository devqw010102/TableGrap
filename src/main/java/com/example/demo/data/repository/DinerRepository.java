package com.example.demo.data.repository;

import com.example.demo.data.model.Diner;
import com.example.demo.data.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DinerRepository extends JpaRepository<Diner, Long> {
    List<Diner> findByCategory(String category);
    Optional<Diner> findById(Long id);

}
