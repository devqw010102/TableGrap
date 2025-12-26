package com.example.demo.data.repository;

import com.example.demo.data.enums.RequestStatus;
import com.example.demo.data.model.Diner;
import com.example.demo.data.model.Member;
import com.example.demo.data.model.Owner;
import com.example.demo.data.model.OwnerRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnerRequestRepository extends JpaRepository<OwnerRequest, Long> {

    boolean existsByOwnerAndDinerAndStatus(Owner owner, Diner diner, RequestStatus status);

//    boolean existsByMemberAndDinerAndStatus(Member member, Diner diner, RequestStatus status);
    Page<OwnerRequest> findAllByStatus(RequestStatus status, Pageable pageable);
}
