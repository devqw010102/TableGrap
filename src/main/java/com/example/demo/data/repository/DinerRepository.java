package com.example.demo.data.repository;

import com.example.demo.data.model.Diner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DinerRepository extends JpaRepository<Diner, Long> {
  List<Diner> findByCategory(String category);
}
