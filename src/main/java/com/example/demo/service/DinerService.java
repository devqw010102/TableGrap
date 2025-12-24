package com.example.demo.service;

import com.example.demo.data.dto.DinerDetailDto;
import com.example.demo.data.dto.DinerListDto;
import com.example.demo.data.dto.owner.OwnerDinerDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DinerService {
    DinerDetailDto getDinerById(Long id);
    Page<DinerListDto> getListByCat(Pageable pageable, String category);
    List<OwnerDinerDto> getOwnerDiners(Long ownerId);
}
