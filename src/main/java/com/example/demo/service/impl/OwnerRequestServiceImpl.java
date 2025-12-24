package com.example.demo.service.impl;

import com.example.demo.data.enums.RequestStatus;
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

    // 권한 신청
    // + List 로 권한 신청을 하기 때문에 Transaction Annotation 추가
    @Override
    @Transactional
    public void requestOwner(Long memberId, List<Long> dinerIds) {

        // member 의 정보는 반복시킬 필요가 없으므로
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("not found member"));

        for(Long dinerId : dinerIds){

            Diner diner = dinerRepository.findById(dinerId).orElseThrow(() -> new IllegalArgumentException("not found diner"));

            if(diner.getOwner() != null) {
                throw new IllegalStateException("이미 사장이 등록된 식당입니다.");
            }

            boolean alreadyRequested = ownerRequestRepository.existsByMemberAndDinerAndStatus(member, diner, RequestStatus.PENDING);
            if(alreadyRequested) {
                throw new IllegalStateException("이미 신청한 식당입니다.");
            }

            OwnerRequest request = OwnerRequest.builder()
                    .member(member)
                    .diner(diner)
                    .status(RequestStatus.PENDING)
                    .build();

            ownerRequestRepository.save(request);
        }
    }
}
