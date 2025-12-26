package com.example.demo.service;

import java.util.List;

public interface OwnerRequestService {
    void requestOwner(Long memberId, List<Long> dinerIds);
}
