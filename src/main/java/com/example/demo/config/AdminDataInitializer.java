package com.example.demo.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminDataInitializer implements CommandLineRunner {
    private final AuthorRepository authorRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    public void run(String... args) throws Exception {
        //1. 관리자 계정이 존재하는지 확인
        if(!authorRepository.existByUsername("admin")){
            //2. 관리자 계정이 존재하지 않으면 생성
            //Author 객체, 리포지터리 확인 할 것
            Author admin = Author.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123")) //비밀번호 암호화
                    .role("ROLE_ADMIN")
                    .build();
            authorRepository.save(admin);
           log.info("관리자 계정이 생성되었습니다.");
        } else {
            log.info("관리자 계정이 이미 존재합니다.");
        }
    }
}
