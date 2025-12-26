package com.example.demo.controller;

import com.example.demo.data.userDeatils.MemberUserDetails;
import com.example.demo.service.OwnerRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/diner")
public class DinerListRestController {
    private final OwnerRequestService ownerRequestService;

    @PostMapping("/ownerRequest")
    public ResponseEntity<String> ownerRequest(
            //js에서 보낸 json데이터를 java에서 Map타입으로 받음
            @RequestBody Map<String, List<Long>> payload,
            @AuthenticationPrincipal MemberUserDetails user
    ) {
        //로그인하지 않았을 경우
        if(user == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인해주세요");
        }
        try {
            List<Long> dinerIds = payload.get("dinerIds");
            //memberId 가져오기
            ownerRequestService.requestOwner(user.getMember().getId(), dinerIds);
            //신청이 정상적으로 진행되면 접수
            return ResponseEntity.ok("신청이 접수되었습니다.");
        } catch(IllegalStateException e){
            //이미 존재하는 신청인 경우
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch(IllegalArgumentException e){
            //잘 못된 데이터
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch(Exception e){
            // 그외 에러
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}