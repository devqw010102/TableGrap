package com.example.demo.data.repository;

import com.example.demo.data.model.Book;
import com.example.demo.data.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book,Long> {

    List<Book> findByMember_id(Long memberId);
}
