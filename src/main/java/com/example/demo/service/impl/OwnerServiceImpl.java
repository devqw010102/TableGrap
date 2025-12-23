package com.example.demo.service.impl;

import com.example.demo.data.dto.owner.OwnerDto;
import com.example.demo.data.model.Authority;
import com.example.demo.data.model.Owner;
import com.example.demo.data.repository.AuthorityRepository;
import com.example.demo.data.repository.OwnerRepository;
import com.example.demo.service.OwnerService;
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

  @Override
  public OwnerDto createOwner(OwnerDto ownerDto) {
    String phoneNumber = ownerDto.getPhone();
    if (phoneNumber != null && phoneNumber.isEmpty()) {
      phoneNumber = null;
    }
      Owner owner = Owner.builder()
              .id(ownerDto.getId())
              .name(ownerDto.getName())
              .username(ownerDto.getUsername())
              .password(passwordEncoder.encode(ownerDto.getPassword()))
              .phone(phoneNumber)
              .build();
      //dinerName 찾기
      ownerRepository.save(owner);

      Authority authority = Authority.builder()
              .authority("ROLE_USER")
              .owner(owner)
              .build();
      authorityRepository.save(authority);
      return mapToOwnerDto(owner, dinerNames);
    }

  @Override
  public Optional<OwnerDto> findByEmail(String email) {
    return ownerRepository.findByEmail(email).map(this::mapToOwnerDto);
  }

  @Override
  public boolean existsByUsername(String username) {
    return ownerRepository.existsByUsername(username);
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
