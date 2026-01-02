package com.example.demo.service.impl;

import com.example.demo.data.dto.notification.OwnerUpdateEvent;
import com.example.demo.data.dto.notification.RegisterEvent;
import com.example.demo.data.dto.notification.ReservationCancelRequestEvent;
import com.example.demo.data.dto.owner.OwnerDto;
import com.example.demo.data.dto.owner.OwnerUpdateDto;
import com.example.demo.data.enums.AccountStatus;
import com.example.demo.data.enums.AuthorityStatus;
import com.example.demo.data.enums.DinerStatus;
import com.example.demo.data.model.Authority;
import com.example.demo.data.model.Book;
import com.example.demo.data.model.Owner;
import com.example.demo.data.repository.*;
import com.example.demo.service.OwnerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor

public class OwnerServiceImpl implements OwnerService {

  private final OwnerRepository ownerRepository;
  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthorityRepository authorityRepository;
  private final DinerRepository dinerRepository;
  private final BookRepository bookRepository;
  private final ApplicationEventPublisher eventPublisher;

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
            .status(AccountStatus.ACTIVE)
            .build();
    ownerRepository.save(owner);

    Authority authority = Authority.builder()
            .authority(AuthorityStatus.ROLE_OWNER.getCode())
            .owner(owner)
            .build();
    authorityRepository.save(authority);

    //2. 식당 정보 조회
    String dinerName = ownerDto.getDinerName();
    String strippedDinerName = dinerName.replace(" ", "");
    //공백 제외하고 식당이름 조회
    dinerRepository.findByDinerNameIgnoreSpaceStatusNot(strippedDinerName, DinerStatus.DELETED)
            .ifPresentOrElse(diner -> {
                //이미 식당 주인이 있는 경우 예외 처리
                if(diner.getOwner() != null) {
                throw new IllegalArgumentException("이미 소유자가 있는 식당입니다");
                }
                diner.setBusinessNum(ownerDto.getBusinessNum());
                diner.setOwner(owner);
                diner.setStatus(DinerStatus.valueOf("PUBLIC"));
            }, () -> {
              // 식당이 존재하지 않을 경우 예외 처리
                throw new IllegalArgumentException(dinerName + "해당 식당이 존재하지 않습니다 ");
            });

    eventPublisher.publishEvent(new RegisterEvent(
            owner.getId(),
            owner.getName(),
            authority.getAuthority()
    ));

    return mapToOwnerDto(owner);
  }
  //이메일 중복확인
  @Override
  public Optional<OwnerDto> findByEmail(String email) {
    Optional<OwnerDto> owner = ownerRepository.findByEmailAndStatus(email, AccountStatus.ACTIVE).map(this::mapToOwnerDto);
    if(owner.isPresent()) return owner;

    return memberRepository.findByEmail(email)
            .map(m -> OwnerDto.builder().email(m.getEmail()).build());
  }

  //아이디 중복확인
  @Override
  public boolean existsByUsername(String username) {
    return ownerRepository.existsByUsername(username, AccountStatus.DELETED) || memberRepository.existsByUsername(username);
  }
  //Owner 회원정보 출력
  @Override
  public Optional<OwnerDto> findByOwnerId(Long id) {
    return ownerRepository.findByIdAndStatus(id, AccountStatus.ACTIVE).map(this::mapToOwnerDto);
  }

  //Owner 회원 정보 수정
  @Override
  @Transactional
  public void updateOwner(Long ownerId, OwnerUpdateDto dto) {
    Owner owner = ownerRepository.findByIdAndStatus(ownerId, AccountStatus.ACTIVE)
            .orElseThrow(() -> new IllegalArgumentException("해당하는 오너가 없습니다."));

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
    eventPublisher.publishEvent(new OwnerUpdateEvent(
            owner.getId(),
            owner.getName()
    ));

    ownerRepository.save(owner);
    mapToOwnerDto(owner);
  }

  @Override
  @Transactional
  public boolean deleteOwner(Long ownerId, String checkPassword) {
    Owner owner = ownerRepository.findByIdAndStatus(ownerId, AccountStatus.ACTIVE)
            .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));
    // 비밀번호 검증
    if (!passwordEncoder.matches(checkPassword, owner.getPassword())) {
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }
    //식당 존재 여부 확인
    if (!dinerRepository.findByOwnerId(ownerId, DinerStatus.DELETED).isEmpty()) {
      throw new IllegalStateException("식당이 존재하는 경우 탈퇴할 수 없습니다.");
    }

    // [추가] 개인정보 마스킹 (재가입 방지 및 개인정보 보호)
    String trashKey = UUID.randomUUID().toString().substring(0, 8); // 랜덤 문자열 생성

    // 아이디/이메일이 Unique라면 충돌나지 않게 랜덤값 mix
    // 예: user123 -> deleted_user123_a1b2c3d4
    owner.setUsername("deleted_" + owner.getUsername() + "_" + trashKey);
    owner.setEmail("deleted_" + trashKey + "@masked.com");

    owner.setName("탈퇴회원"); // 이름은 식별 불가능하게
    owner.setPhone("000-0000-0000"); // 전화번호 초기화
    owner.setPassword(""); // 비밀번호 삭제 (혹은 임의의 값으로 변경)
    owner.setStatus(AccountStatus.DELETED);

    Authority authority = authorityRepository.findByOwner(owner).getFirst();
    authority.setAuthority(AuthorityStatus.ROLE_DELETED.getCode());
    authorityRepository.save(authority);

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

  @Override
  public void notificationUser(Long bookId) {
    Optional<Book> bookOp = bookRepository.findById(bookId);
    bookOp.ifPresent(book -> {
      // 이벤트 보내기
      DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
      String formatterDate = book.getBookingDate().format(dtf);

      eventPublisher.publishEvent(new ReservationCancelRequestEvent(book.getMember().getId(), book.getDiner().getDinerName(), formatterDate));
    });

  }

  // 중복 확인이나 단순 조회용 (Owner 객체만 있을 때)
  public OwnerDto mapToOwnerDto(Owner owner) {
    // 식당 이름이 필요 없으므로 빈 리스트를 넘겨서 위의 기존 메서드를 재사용
    return mapToOwnerDto(owner, Collections.emptyList());
  }

    public static class CancelManager {
        public static final Set<Long> allowedBookingIds = Collections.synchronizedSet(new HashSet<>());
  }
}
