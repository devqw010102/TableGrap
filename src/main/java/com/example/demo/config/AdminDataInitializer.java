package com.example.demo.config;

import com.example.demo.data.model.Authority;
import com.example.demo.data.model.Member;
import com.example.demo.data.repository.MemberRepository;
import com.example.demo.data.repository.AuthorityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminDataInitializer implements CommandLineRunner {

    // 두 개의 리포지토리가 필요합니다.
    private final MemberRepository memberRepository;
    private final AuthorityRepository authorityRepository;

    private final PasswordEncoder passwordEncoder;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @Value("${admin.name}")
    private String adminName;

    @Override
    @Transactional // 두 번의 저장이 일어나므로 트랜잭션으로 묶는 게 안전합니다.
    public void run(String... args) throws Exception {

        // 1. Member 테이블에서 관리자 존재 여부 확인
        if (!memberRepository.existsByUsername(adminUsername)) {

            log.info("[AdminInitializer] 관리자 계정 생성 시작: {}", adminUsername);

            // ---------------------------------------------------
            // STEP 1: Member (회원) 정보 저장
            // ---------------------------------------------------
            Member admin = Member.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .name(adminName)
                    // .role(...) -> 이 부분 삭제됨! Member에는 role 필드가 없을 테니까요.
                    .build();

            // 중요: 먼저 저장해야 admin 객체에 ID(PK)가 생성됩니다.
            Member savedAdmin = memberRepository.save(admin);

            // ---------------------------------------------------
            // STEP 2: Authority (권한) 정보 저장
            // ---------------------------------------------------
            Authority adminAuthority = Authority.builder()
                    .member(savedAdmin)      // 방금 저장한 관리자 객체 연결 (FK 설정)
                    .authority("ROLE_ADMIN") // ★ 원하시는 "ROLE_ADMIN" 문자열 입력
                    .build();

            authorityRepository.save(adminAuthority);

            log.info("[AdminInitializer] 관리자 계정 및 권한(ROLE_ADMIN) 생성 완료.");
        } else {
            log.info("[AdminInitializer] 관리자 계정이 이미 존재합니다.");
        }
    }
}
