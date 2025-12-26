package com.example.demo.controller;

import com.example.demo.data.dto.DinerDetailDto;
import com.example.demo.data.dto.DinerListDto;
import com.example.demo.data.model.Member;
import com.example.demo.service.DinerService;
import com.example.demo.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/diner")
public class DinerListController {
  private final DinerService dinerService;
    private final MemberService memberService;

    //카테고리별로 일일이 매핑하는 것은 비효율적이므로 쿼리 파라미터 사용
  @GetMapping("/list")
  public String getDiner(@PageableDefault(size = 20, sort="id", direction = Sort.Direction.ASC) Pageable pageable,
                         @RequestParam("category") String category, Model model) {
    Page<DinerListDto> page = dinerService.getListByCat(pageable, category);
    model.addAttribute("dinerList", page);
    model.addAttribute("categoryName", category);
    return "dinerList/dinerList";
  }

  @PreAuthorize("hasAnyRole('USER', 'OWNER')")
  @GetMapping("/reservation")
  public String reserveDiner(@RequestParam("id") Long id, Authentication authentication) {

      if (authentication == null || !authentication.isAuthenticated()) {
          return "redirect:/login"; // 로그인 안 했으면 로그인 페이지로
      }
      boolean isOwner = authentication.getAuthorities().stream()
              .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));

      if(isOwner) {
          log.info("점주는 권한이 제한됩니다.");
          return "redirect:/";
      }

      Member member = memberService.getMember(authentication.getName());
      return "reservation/reservation";
  }
}
