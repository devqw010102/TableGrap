package com.example.demo.controller;

import com.example.demo.data.dto.DinerListDto;
import com.example.demo.data.model.Diner;
import com.example.demo.data.model.MemberUserDetails;
import com.example.demo.service.DinerService;
import com.example.demo.service.impl.CustomUserDetailsService;
import com.example.demo.service.impl.DinerServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Internal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;

import java.util.List;
import java.util.Map;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/diner")
public class DinerListController {
  private final DinerService dinerService;
  //지금 브랜치에 OwnerRequestService가 없음
  private final OwnerRequestSerivce ownerRequestSerivce;

  //카테고리별로 일일이 매핑하는 것은 비효율적이므로 쿼리 파라미터 사용
  @GetMapping("/list")
  public String getDiner(@PageableDefault(size = 20, sort="id", direction = Sort.Direction.ASC) Pageable pageable,
                         @RequestParam("category") String category, Model model) {
    Page<DinerListDto> page = dinerService.getListByCat(pageable, category);
    model.addAttribute("dinerList", page);
    model.addAttribute("categoryName", category);
    return "dinerList/dinerList";
  }
  @PostMapping("/ownerRequest")
  public ResponseEntity<String> ownerRequest(
          //프런트엔드에서 js 보낸 json데이터를 java에서 Map타입으로 받음
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
            ownerRequestSerivce.requestOwner(user.getMemberId(), dinerIds);
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
