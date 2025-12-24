package com.example.demo.controller;

import com.example.demo.data.dto.owner.OwnerDto;
import com.example.demo.service.OwnerService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
public class OwnerRegController {
    private final OwnerService ownerService;

    @Value("${bizno.key}")
    private String biznoKey;
    private final RestClient restClient = RestClient.create();

    // api키 proxy활용으로 대신 조회
    @GetMapping("/proxy/business-info")
    public ResponseEntity<?> getBusinessInfo(@RequestParam("query") String businessNumber) {

        // Bizno API 호출 URL 구성 (서버에 숨겨진 Key 사용)
        String externalUrl = "https://bizno.net/api/fapi?key=" + biznoKey
                + "&gb=1"
                + "&q=" + businessNumber
                + "&type=json"
                + "&pagecnt=1";

        try {
            // 스프링 서버가 Bizno에 대신 요청
            String responseBody = restClient.get()
                    .uri(externalUrl)
                    .retrieve()
                    .body(String.class);

            // 결과를 프론트엔드에 그대로 전달 (JSON 형태)
            return ResponseEntity.ok(responseBody);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("사업자 조회 중 서버 오류 발생");
        }
    }
    // ---------------------------------------------------------


    //owner 회원가입
    @PostMapping("/register")
    @Transactional
    public ResponseEntity<?> register(@Valid @RequestBody OwnerDto ownerDto, BindingResult bindingResult) {
        if(bindingResult.hasErrors()){
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(","));
            return ResponseEntity.badRequest().body(errorMessage);
        }

        if (ownerService.existsByUsername(ownerDto.getUsername())) {
            return ResponseEntity.badRequest().body("Username is already in use");
        }

        if (ownerService.findByEmail(ownerDto.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email is already in use");
        }
        if (!ownerDto.getPassword().equals(ownerDto.getPasswordConfirm())) {
            return ResponseEntity.badRequest().body("Passwords do not match");
        }
        ownerService.createOwner(ownerDto);
        return ResponseEntity.ok("회원 가입이 완료되었습니다.");
    }

    // Email double check
    @GetMapping("/check-email")
    public String checkEmail(@RequestParam String email) {
        boolean isDuplicate = ownerService.findByEmail(email).isPresent();
        return isDuplicate ? "이미 사용 중인 이메일입니다." : "사용 가능한 이메일입니다.";
    }

    // ID double check
    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
        return ResponseEntity.ok(ownerService.existsByUsername(username));
    }
}
