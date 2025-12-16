package com.example.demo.data.repository;

import com.example.demo.data.enums.RequestStatus;
import com.example.demo.data.model.OwnerRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnerRequestRepository extends JpaRepository<OwnerRequest, Long> {

    boolean existsByMemberIdAndDinerIdAndStatus(Long memberId, Long dinerId, RequestStatus status);
}
