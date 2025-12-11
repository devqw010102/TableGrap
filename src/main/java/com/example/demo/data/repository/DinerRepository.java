package com.example.demo.data.repository;

import com.example.demo.data.model.Diner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DinerRepository extends JpaRepository<Diner, Long> {

}
