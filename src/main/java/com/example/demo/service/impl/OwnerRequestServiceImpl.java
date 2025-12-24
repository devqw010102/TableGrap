package com.example.demo.service.impl;

import com.example.demo.data.dto.owner.OwnerRequestDto;
import com.example.demo.data.enums.RequestStatus;
import com.example.demo.data.model.*;
import com.example.demo.data.repository.*;
import com.example.demo.service.OwnerRequestService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OwnerRequestServiceImpl implements OwnerRequestService {

    private final OwnerRequestRepository ownerRequestRepository;
    private final MemberRepository memberRepository;
    private final DinerRepository dinerRepository;
    private final AuthorityRepository authorityRepository;
    private final OwnerRepository ownerRepository;

    // 권한 신청
    // + List 로 권한 신청을 하기 때문에 Transaction Annotation 추가
    @Override
    @Transactional
    public void requestOwner(Long ownerId, List<Long> dinerIds) {

        // member 의 정보는 반복시킬 필요가 없으므로
        Owner owner = ownerRepository.findById(ownerId).orElseThrow(() -> new IllegalArgumentException("not found member"));

        for(Long dinerId : dinerIds){

            Diner diner = dinerRepository.findById(dinerId).orElseThrow(() -> new IllegalArgumentException("not found diner"));

            if(diner.getOwner() != null) {
                throw new IllegalStateException("이미 사장이 등록된 식당입니다.");
            }

            boolean alreadyRequested = ownerRequestRepository.existsByOwnerAndDinerAndStatus(owner, diner, RequestStatus.PENDING);
            if(alreadyRequested) {
                throw new IllegalStateException("이미 신청한 식당입니다.");
            }

            OwnerRequest request = OwnerRequest.builder()
                    .owner(owner)
                    .diner(diner)
                    .status(RequestStatus.PENDING)
                    .build();

            ownerRequestRepository.save(request);
        }
    }

    // 권한 신청 목록
    @Override
    public List<OwnerRequestDto> findAll() {
        return ownerRequestRepository.findAll().stream().map(OwnerRequestDto::from).toList();
    }

    // 승인 처리
    @Override
    public void approve(Long requestId) {
        OwnerRequest request = ownerRequestRepository.findById(requestId).orElseThrow(() -> new IllegalArgumentException("not found request"));

        if(request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 신청입니다.");
        }

        Diner diner = request.getDiner();
        Owner owner = request.getOwner();

        if(diner.getOwner() != null) {
            throw new IllegalStateException("이미 사장이 등록된 식당입니다.");
        }

        // Status 승인으로, Diner entity 에도 owner 값 추가
        request.setStatus(RequestStatus.APPROVED);
        diner.setOwner(owner);

        // Authority 에 해당 아이디가 'ROLE_OWNER'를 가지고 있다면 추가 X
        boolean hasOwnerRole = authorityRepository.existsByMemberAndAuthority(owner, "ROLE_OWNER");
        if(!hasOwnerRole) {
            Authority ownerAuthority = Authority.builder()
                    .owner(owner)
                    .authority("ROLE_OWNER")
                    .build();

            authorityRepository.save(ownerAuthority);
        }
    }

    // 반려 처리
    @Override
    public void reject(Long requestId) {
        OwnerRequest request = ownerRequestRepository.findById(requestId).orElseThrow(() -> new IllegalArgumentException("not found request"));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 신청입니다.");
        }

        request.setStatus(RequestStatus.REJECTED);
    }
}
