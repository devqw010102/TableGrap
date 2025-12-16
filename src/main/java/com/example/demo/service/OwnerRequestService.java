package com.example.demo.service;

import com.example.demo.data.dto.OwnerRequestDto;

import java.util.List;

public interface OwnerRequestService {

    void requestOwner(Long memberId, Long dinerId);

    List<OwnerRequestDto> findAll();

    void approve(Long requestId);

    void reject(Long requestId);
}
