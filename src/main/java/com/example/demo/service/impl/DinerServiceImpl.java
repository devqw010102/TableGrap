package com.example.demo.service.impl;

import com.example.demo.data.dto.DinerDetailDto;
import com.example.demo.data.dto.DinerDto;
import com.example.demo.data.dto.DinerListDto;
import com.example.demo.data.dto.owner.OwnerDinerDto;
import com.example.demo.data.dto.admin.AdminOwnerDto;
import com.example.demo.data.dto.owner.OwnerDto;
import com.example.demo.data.model.Diner;
import com.example.demo.data.model.Owner;
import com.example.demo.data.repository.BookRepository;
import com.example.demo.data.repository.DinerRepository;
import com.example.demo.data.repository.OwnerRepository;
import com.example.demo.service.DinerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DinerServiceImpl implements DinerService {

    private final DinerRepository dinerRepository;
    private final BookRepository bookRepository;
    private final OwnerRepository ownerRepository;

    // Diner -> DinerListDto 변환 메서드
    private DinerListDto mapToDinerListDto(Diner diner){
        return DinerListDto.builder()
                .id(diner.getId())
                .dinerName(diner.getDinerName())
                .category(diner.getCategory())
                .build();
    }

    //Diner -> DinerDto 변환 메서드
    private DinerDto mapToDinerDto(Diner diner){
        return DinerDto.builder()
                .owner(diner.getOwner())
                .businessNum(diner.getBusinessNum())
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

    //식당 추가
    @Override
    @Transactional
    public void addDiner(DinerDto dto, String username) {
        //공백 제외하고 식당이름 조회
        String dinerName = dto.getDinerName();
        String strippedDinerName = dinerName.replace(" ", "");
        //로그인한 owner계정 조회
        Owner owner = ownerRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        //식당이름으로 기존 owner가 존재하는지 확인
        dinerRepository.findByDinerNameIgnoreSpace(strippedDinerName)
                .ifPresentOrElse(diner -> {
                    //이미 식당 주인이 있는 경우 예외 처리
                    if(diner.getOwner() != null) {
                        throw new IllegalArgumentException("이미 소유자가 있는 식당입니다");
                    }
                    diner.setBusinessNum(dto.getBusinessNum());
                    diner.setOwner(owner);
                }, () -> {
                    // 식당이 존재하지 않을 경우 예외 처리
                    throw new IllegalArgumentException(dinerName + "해당 식당이 존재하지 않습니다 ");
                });
    }

    //식당 삭제 탭에서 선택한 식당 출력
    @Override
    public Optional<OwnerDinerDto> getOwnerDinerById(Long dinerId, Long ownerId) {
        return dinerRepository.findDinerByOwner(dinerId, ownerId);
    }

    //식당 삭제
    @Override
    @Transactional
    public void deleteDiner(Long dinerId, Long ownerId) {
        Diner diner = dinerRepository.findByIdAndOwnerId(dinerId, ownerId).orElseThrow(() -> new IllegalArgumentException("식당을 찾을 수 없습니다."));
        //삭제신청일 이후의 예약이 존재하는 지 확인
        boolean hasFutureBookings = bookRepository.existsByDiner_IdAndBookingDateAfter(dinerId, LocalDateTime.now());
        if (hasFutureBookings) {
            throw new IllegalStateException("예약일자가 지나지 않은 예약이 존재하여 삭제할 수 없습니다.");
        }
        dinerRepository.delete(diner);
        //실제로 db를 지우는 것이 아닌 diner 테이블의 ownerId만 삭제하는 방법...?
        //diner.setOwner(null);
    }


    @Transactional
    @Override
    public List<AdminOwnerDto> getAll() {
        return dinerRepository.findOwnerDiners().stream().map(d -> AdminOwnerDto.builder()
                        .memberId(d.getOwner().getId())
                        .ownerName(d.getOwner().getName())
                        .email(d.getOwner().getEmail())
                        .phone(d.getOwner().getPhone())
                        .dinerId(d.getId())
                        .dinerName(d.getDinerName())
                        .category(d.getCategory())
                        .status(d.getStatus().toString())
                        .build())
                .toList();
    }
}
