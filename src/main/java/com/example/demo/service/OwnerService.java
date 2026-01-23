package com.example.demo.service;

import com.example.demo.data.dto.ReviewChartDto;
import com.example.demo.data.dto.RevisitChartDto;
import com.example.demo.data.dto.owner.OwnerDto;
import com.example.demo.data.dto.owner.OwnerUpdateDto;

import java.util.List;
import java.util.Optional;

public interface OwnerService {

  OwnerDto createOwner(OwnerDto ownerDto);

  Optional<OwnerDto> findByEmail(String email);

  boolean existsByUsername(String username);

  Optional<OwnerDto> findByOwnerId(Long id);

  void updateOwner(Long id, OwnerUpdateDto ownerUpdateDto);

  boolean deleteOwner(Long ownerId, String checkPassword);

  void notificationUser(Long bookId);

//  List<ReviewChartDto> getAvgRate(Long ownerId);

  String generateReviewChart(Long ownerId);

//  List<RevisitChartDto> getRevisits(Long ownerId);

  String generateRevisitsChart(Long ownerId);

//  List<RevisitChartDto> getBookByDinerId(Long dinerId, Long ownerId);

  String genRevisitsChartByDiner(Long dinerId, Long ownerId);

  String genReviewChartByDiner(Long dinerId, Long ownerId);
}
