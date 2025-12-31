package com.example.demo.controller;

import com.example.demo.data.dto.owner.OwnerDto;
import com.example.demo.data.model.Diner;
import com.example.demo.data.repository.OwnerRepository;
import com.example.demo.service.DinerService;
import com.example.demo.service.OwnerService;
import com.example.demo.service.impl.OwnerServiceImpl;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
public class OwnerRegController {
    private final OwnerService ownerService;
    private final DinerService dinerService;
    private final ObjectMapper objectMapper;

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
        /* 도저히 감이 안잡혀서 GeoCodingService랑 GEMINI를 참고해서 작성함
         * 다른 방법으로 json을 받는 dto를 만드는 방법이 있는데 단순히 db와 식당명과 일치하기 위해 사용하기 때문에
         * objectMapper를 사용함*/
        try {
            // 스프링 서버가 Bizno에 대신 요청
            String responseBody = restClient.get()
                    .uri(externalUrl)
                    .retrieve()
                    .body(String.class);
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode itemsNode = rootNode.path("items");
            // 아이템이 있는지 확인 (검색 결과가 존재하는지)
            if (itemsNode.isArray() && !itemsNode.isEmpty()) {

                // 첫 번째 배열 요소의 "company" 값을 문자열로 가져오기
                String apiCompanyName = itemsNode.get(0).path("company").asText();

                System.out.println("API 조회된 상호명: " + apiCompanyName); // 확인용

                //DB 매칭 로직
                Optional<Diner> dinerOptional = dinerService.findByDinerNameBiz(apiCompanyName);
                if (dinerOptional.isPresent()) {
                    // 성공 시: Diner 객체 반환
                    return ResponseEntity.ok(dinerOptional.get());
                } else {
                    // 실패 시: 문자열 메시지 반환
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("조회된 사업자 정보(" + apiCompanyName + ")가 우리 서비스에 등록되지 않았습니다.");
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 사업자 번호로 조회된 정보가 없습니다.");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.internalServerError().body("조회 중 오류 발생: " + e.getMessage());
        }
    }
    // ---------------------------------------------------------

    //owner 회원가입
    @PostMapping("/register")
    @Transactional
    public ResponseEntity<?> register(@Valid @RequestBody OwnerDto ownerDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
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

    // owner cancel allowed
    @PatchMapping("/{bookId}/allow-cancel")
    public ResponseEntity<String> allowCancel(@PathVariable Long bookId) {
        // static Set에 ID 추가
        OwnerServiceImpl.CancelManager.allowedBookingIds.add(bookId);
        return ResponseEntity.ok("일시적으로 취소 권한이 부여되었습니다.");
    }
}
