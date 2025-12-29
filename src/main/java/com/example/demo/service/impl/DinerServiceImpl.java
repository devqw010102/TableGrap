package com.example.demo.service.impl;

import com.example.demo.data.dto.DinerDetailDto;
import com.example.demo.data.dto.DinerDto;
import com.example.demo.data.dto.DinerListDto;
import com.example.demo.data.dto.owner.OwnerDinerDto;
import com.example.demo.data.enums.DinerStatus;
import com.example.demo.data.model.Diner;
import com.example.demo.data.model.Owner;
import com.example.demo.data.repository.BookRepository;
import com.example.demo.data.repository.DinerRepository;
import com.example.demo.data.repository.OwnerRepository;
import com.example.demo.service.DinerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
                .status(diner.getStatus())
                .ownerPhone(diner.getOwner() != null ? diner.getOwner().getPhone() : null)
                .build();
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
        return dinerRepository.findByCategoryAndStatusNot(pageable, category, DinerStatus.DELETED).map(this::mapToDinerListDto);
    }

    @Override
    public List<OwnerDinerDto> getOwnerDiners(Long ownerId) {
        return dinerRepository.findByOwnerId(ownerId, DinerStatus.DELETED);
    }

    //사업자 조회시 식당 추가
    @Override
    public Optional<Diner> findByDinerNameBiz(String dinerName){
        return dinerRepository.findByDinerNameIgnoreSpaceStatusNot(dinerName, DinerStatus.DELETED);
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
        dinerRepository.findByDinerNameIgnoreSpaceStatusNot(strippedDinerName, DinerStatus.DELETED)
                .ifPresentOrElse(diner -> {
                    //이미 식당 주인이 있는 경우 예외 처리
                    if(diner.getOwner() != null) {
                        throw new IllegalArgumentException("이미 소유자가 있는 식당입니다");
                    }
                    diner.setBusinessNum(dto.getBusinessNum());
                    diner.setOwner(owner);
                    diner.setStatus(DinerStatus.valueOf("PUBLIC"));
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
        //예약일자가 지나고 실제로 삭제되는 확인: LocalDateTime.now() -> LocalDateTime.of()로 변경하고 테스트
        boolean hasFutureBookings = bookRepository.existsByDiner_IdAndBookingDateAfter(dinerId, LocalDateTime.of(2026, 1, 10, 0, 0));
        if (hasFutureBookings) {
            throw new IllegalStateException("예약일자가 지나지 않은 예약이 존재하여 삭제할 수 없습니다.");
        }
        // 식당을 삭제하기 전에, 이 식당의 '과거 예약 내역'들을 먼저 모두 삭제해야 함(Hard Delete)
        //bookRepository.deleteAllByDiner_Id(dinerId);
        //dinerRepository.delete(diner);

        //enum활용하여 status를 delete로 변경(soft delete)
        diner.setStatus(DinerStatus.DELETED);
        //식당목록을 불러오는 메소드 변경 필요


    }

    //식당 상태 변경
    @Override
    @Transactional
    public void changeStatus(Long dinerId, Long ownerId) {
        Diner diner = dinerRepository.findByIdAndOwnerId(dinerId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("식당이 존재하지 않습니다."));
        if(diner.getStatus().equals(DinerStatus.PUBLIC)) {
            diner.setStatus(DinerStatus.CLOSED);
        } else if(diner.getStatus().equals(DinerStatus.CLOSED)) {
            diner.setStatus(DinerStatus.PUBLIC);
        }
    }
}
