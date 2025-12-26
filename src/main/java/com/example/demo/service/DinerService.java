package com.example.demo.service;

import com.example.demo.data.dto.DinerDetailDto;
import com.example.demo.data.dto.DinerDto;
import com.example.demo.data.dto.DinerListDto;
import com.example.demo.data.dto.owner.OwnerDinerDto;
import com.example.demo.data.dto.admin.AdminOwnerDto;
import com.example.demo.data.dto.owner.OwnerDto;
import com.example.demo.data.model.Diner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface DinerService {

    //예약 페이지에서 식당 내용 출력
    DinerDetailDto getDinerById(Long id);
    Page<DinerListDto> getListByCat(Pageable pageable, String category);
    List<OwnerDinerDto> getOwnerDiners(Long ownerId);
    //식당 추가
    void addDiner(DinerDto dto, String username);
    //식당 삭제 탭에서 식당 출력
    Optional<OwnerDinerDto> getOwnerDinerById(Long id, Long ownerId);
    //식당 삭제
    void deleteDiner(Long id, Long ownerId);
//    List<AdminOwnerDto> getAll();
}
