package com.example.demo.service;

import com.example.demo.data.dto.owner.OwnerDto;
import com.example.demo.data.dto.owner.OwnerUpdateDto;

import java.util.Optional;

public interface OwnerService {

  OwnerDto createOwner(OwnerDto ownerDto);

  Optional<OwnerDto> findByEmail(String email);

  boolean existsByUsername(String username);

  Optional<OwnerDto> findByOwnerId(Long id);

  void updateOwner(Long id, OwnerUpdateDto ownerUpdateDto);

  boolean deleteOwner(Long ownerId, String checkPassword);
}
