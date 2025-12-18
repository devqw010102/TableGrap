package com.example.demo.service.impl;

import com.example.demo.data.dto.DinerDetailDto;
import com.example.demo.data.dto.DinerListDto;
import com.example.demo.data.dto.OwnerDinerDto;
import com.example.demo.data.model.Diner;
import com.example.demo.data.repository.DinerRepository;
import com.example.demo.service.DinerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DinerServiceImpl implements DinerService {

    private final DinerRepository dinerRepository;
    // Diner -> DinerListDto 변환 메서드
    private DinerListDto mapToDinerListDto(Diner diner){
        return DinerListDto.builder()
                .id(diner.getId())
                .dinerName(diner.getDinerName())
                .category(diner.getCategory())
                .build();
    }

    //Diner -> DinerDetailDto 변환 메서드
    private DinerDetailDto mapToDinerDetailDto(Diner diner){
        return DinerDetailDto.builder()
                .id(diner.getId())
                .dinerName(diner.getDinerName())
                .category(diner.getCategory())
                .location(diner.getLocation())
                .tel(diner.getTel())
                .dx(diner.getDx())
                .dy(diner.getDy())
                .build();
    }

    public List<DinerDetailDto> getList() {
        return dinerRepository.findAll().stream().map(this::mapToDinerDetailDto).toList();
    }

    @Override
    public DinerDetailDto getDinerById(Long id) {
      // DB에서 ID로 찾고 DinerDetailDto로 변환, 없으면 에러
      return dinerRepository.findById(id).map(this::mapToDinerDetailDto)
              .orElseThrow(() -> new IllegalArgumentException("해당 식당이 없습니다. id=" + id));
    }
    
    @Override
    //Pagination활용하기 위해 Page타입으로 변경
    public Page<DinerListDto> getListByCat(Pageable pageable, String category){
      return dinerRepository.findByCategory(pageable, category).map(this::mapToDinerListDto);
    }

    @Override
    public List<OwnerDinerDto> getOwnerDiners(Long ownerId) {
        return dinerRepository.findByOwnerId(ownerId);
    }
}
