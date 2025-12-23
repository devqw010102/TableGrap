package com.example.demo.service;

import com.example.demo.data.dto.owner.OwnerDto;

import java.util.Optional;

public interface OwnerService {
  OwnerDto createOwner(OwnerDto ownerDto);
  Optional<OwnerDto> findByEmail(String email);
  boolean existsByUsername(String username);
}
