package com.example.demo.data.repository;

import com.example.demo.data.model.DinerAvailability;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

public interface DinerAvailabilityRepository extends JpaRepository<DinerAvailability, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE) // 수정 중에는 다른 트랜잭션이 접근 못하게 잠금
    @Query("select a from DinerAvailability a where a.diner.id = :dinerId and a.date = :date and a.time = :time")
    Optional<DinerAvailability> findByDinerIdAndDateAndTime(Long dinerId, LocalDate date, LocalTime time);

    @Modifying
    @Transactional      // 시간 지나면 슬롯 자동 삭제
    void deleteByDateBefore(LocalDate date);
}
