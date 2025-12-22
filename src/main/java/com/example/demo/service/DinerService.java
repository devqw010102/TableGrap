package com.example.demo.service;

import com.example.demo.data.dto.DinerDetailDto;
import com.example.demo.data.dto.DinerListDto;
import com.example.demo.data.dto.OwnerDinerDto;
import com.example.demo.data.dto.admin.AdminOwnerDto;
import com.example.demo.data.model.Diner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DinerService {
    public List<DinerDetailDto> getList();
    DinerDetailDto getDinerById(Long id);
    public Page<DinerListDto> getListByCat(Pageable pageable, String category);
    List<OwnerDinerDto> getOwnerDiners(Long ownerId);

    List<AdminOwnerDto> getAll();
}
