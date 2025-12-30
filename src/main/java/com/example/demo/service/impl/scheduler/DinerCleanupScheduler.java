package com.example.demo.service.impl.scheduler;

import com.example.demo.data.repository.DinerAvailabilityRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DinerCleanupScheduler {

    private final DinerAvailabilityRepository availabilityRepository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional  // 매일 새벽 3시 마다 슬롯 자동 삭제
    public void cleanupOldSlot() {
        LocalDate date = LocalDate.now();
        availabilityRepository.deleteByDateBefore(date);
    }

}
