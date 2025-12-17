package com.example.demo.service.impl;

import com.example.demo.data.dto.OwnerRequestDto;
import com.example.demo.data.enums.RequestStatus;
import com.example.demo.data.model.Authority;
import com.example.demo.data.model.Diner;
import com.example.demo.data.model.Member;
import com.example.demo.data.model.OwnerRequest;
import com.example.demo.data.repository.AuthorityRepository;
import com.example.demo.data.repository.DinerRepository;
import com.example.demo.data.repository.MemberRepository;
import com.example.demo.data.repository.OwnerRequestRepository;
import com.example.demo.service.OwnerRequestService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Request;
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

    @Override
    public void requestOwner(Long memberId, Long dinerId) {
        if(ownerRequestRepository.existsByMemberIdAndDinerIdAndStatus(memberId, dinerId, RequestStatus.PENDING)) {
            throw new IllegalStateException("이미 신청한 식당입니다.");
        }

        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("not found member"));
        Diner diner = dinerRepository.findById(dinerId).orElseThrow(() -> new IllegalArgumentException("not found diner"));

        OwnerRequest request = OwnerRequest.builder()
                .member(member)
                .diner(diner)
                .status(RequestStatus.PENDING)
                .build();

        ownerRequestRepository.save(request);
    }

    @Override

    public List<OwnerRequestDto> findAll() {
        return ownerRequestRepository.findAll().stream().map(OwnerRequestDto::from).toList();
    }

    @Override
    public void approve(Long requestId) {
        OwnerRequest request = ownerRequestRepository.findById(requestId).orElseThrow(() -> new IllegalArgumentException("not found request"));

        if(request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 신청입니다.");
        }

        request.setStatus(RequestStatus.APPROVED);

        Authority ownerAuthority = Authority.builder()
                .authority("ROLE_OWNER")
                .member(request.getMember())
                .build();

        authorityRepository.save(ownerAuthority);
    }

    @Override
    public void reject(Long requestId) {
        OwnerRequest request = ownerRequestRepository.findById(requestId).orElseThrow(() -> new IllegalArgumentException("not found request"));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 신청입니다.");
        }

        request.setStatus(RequestStatus.REJECTED);
    }
}
