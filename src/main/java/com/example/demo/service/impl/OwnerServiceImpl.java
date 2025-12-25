package com.example.demo.service.impl;

import com.example.demo.data.dto.owner.OwnerDto;
import com.example.demo.data.dto.owner.OwnerUpdateDto;
import com.example.demo.data.model.Authority;
import com.example.demo.data.model.Owner;
import com.example.demo.data.repository.AuthorityRepository;
import com.example.demo.data.repository.DinerRepository;
import com.example.demo.data.repository.OwnerRepository;
import com.example.demo.service.OwnerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor

public class OwnerServiceImpl implements OwnerService {
  private final OwnerRepository ownerRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthorityRepository authorityRepository;
  private final DinerRepository dinerRepository;

  @Override
  @Transactional
  public OwnerDto createOwner(OwnerDto ownerDto) {
    //1. Owner 저장
    Owner owner = Owner.builder()
            .id(ownerDto.getId())
            .name(ownerDto.getName())
            .username(ownerDto.getUsername())
            .password(passwordEncoder.encode(ownerDto.getPassword()))
            .phone(ownerDto.getPhone())
            .email(ownerDto.getEmail())
            .build();
    ownerRepository.save(owner);

    Authority authority = Authority.builder()
            .authority("ROLE_OWNER")
            .owner(owner)
            .build();
    authorityRepository.save(authority);

    //2. 식당 정보 조회
    String dinerName = ownerDto.getDinerName();
    String strippedDinerName = dinerName.replace(" ", "");
    //공백 제외하고 식당이름 조회
    dinerRepository.findByDinerNameIgnoreSpace(strippedDinerName)
            .ifPresentOrElse(diner -> {
              //이미 식당 주인이 있는 경우 예외 처리
              if(diner.getOwner() != null) {
                throw new IllegalArgumentException("이미 소유자가 있는 식당입니다");
              }
              diner.setBusinessNum(ownerDto.getBusinessNum());
              diner.setOwner(owner);
            }, () -> {
              // 식당이 존재하지 않을 경우 예외 처리
              throw new IllegalArgumentException(dinerName + "해당 식당이 존재하지 않습니다 ");
            });
    return mapToOwnerDto(owner);
  }
  //이메일 중복확인
  @Override
  public Optional<OwnerDto> findByEmail(String email) {
    return ownerRepository.findByEmail(email).map(this::mapToOwnerDto);
  }
  //아이디 중복확인
  @Override
  public boolean existsByUsername(String username) {
    return ownerRepository.existsByUsername(username);
  }
  //Owner 회원정보 출력
  @Override
  public Optional<OwnerDto> findByOwnerId(Long id) {
    return ownerRepository.findById(id).map(this::mapToOwnerDto);
  }

  //Owner 회원 정보 수정
  @Override
  @Transactional
  public void updateOwner(Long ownerId, OwnerUpdateDto dto) {
    Owner owner = ownerRepository.findById(ownerId)
            .orElseThrow(() -> new IllegalArgumentException("해당하는 오너가 없습니다."));
    System.out.println("=== UPDATE REQUEST ===");
    System.out.println("Email: " + dto.getEmail());
    System.out.println("Phone: " + dto.getPhone());
    System.out.println("Password: " + dto.getPassword());

    if(dto.getEmail() != null && !dto.getEmail().isBlank()) {
      owner.setEmail(dto.getEmail());
      System.out.println(">> Email Changed to: " + dto.getEmail()); // 변경 로그
    }
    if(dto.getPhone() != null && !dto.getPhone().isBlank()) {
      owner.setPhone(dto.getPhone());
      System.out.println(">> Phone Changed to: " + dto.getPhone()); // 변경 로그
    }
    if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
      owner.setPassword(passwordEncoder.encode(dto.getPassword()));
      System.out.println(">> Password Changed"); // 변경 로그
    }
    ownerRepository.save(owner);
    mapToOwnerDto(owner);
  }


  //Owner 계정 삭제
  /*public void deleteOwnerById(Long id) {
    if (!dinerRepository.findByOwnerId(id).isEmpty()) {
      throw new IllegalStateException("식당이 존재하는 경우 탈퇴할 수 없습니다.");
    } else {
      ownerRepository.deleteById(id);
    }
  } */
  @Override
  public boolean deleteOwner(Long ownerId, String checkPassword) {
    Owner owner = ownerRepository.findById(ownerId)
            .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));
    // 비밀번호 검증
    if (!passwordEncoder.matches(checkPassword, owner.getPassword())) {
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }
    //식당 존재 여부 확인
    if (!dinerRepository.findByOwnerId(ownerId).isEmpty()) {
      throw new IllegalStateException("식당이 존재하는 경우 탈퇴할 수 없습니다.");
    }
    ownerRepository.delete(owner);
    return true;
  }

  public OwnerDto mapToOwnerDto(Owner owner, List<String> dinerNames) {
    return OwnerDto.builder()
            .id(owner.getId())
            .name(owner.getName())
            .username(owner.getUsername())
            .password(owner.getPassword())
            .email(owner.getEmail())
            .phone(owner.getPhone())
            .dinerNames(dinerNames)
            .build();
  }

  // 중복 확인이나 단순 조회용 (Owner 객체만 있을 때)
  public OwnerDto mapToOwnerDto(Owner owner) {
    // 식당 이름이 필요 없으므로 빈 리스트를 넘겨서 위의 기존 메서드를 재사용
    return mapToOwnerDto(owner, Collections.emptyList());
  }
}
