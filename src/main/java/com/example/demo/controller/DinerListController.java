package com.example.demo.controller;

import com.example.demo.data.dto.DinerDetailDto;
import com.example.demo.data.dto.DinerListDto;
import com.example.demo.data.enums.AuthorityStatus;
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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Objects;

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

    // owner면 dinerList에서 home으로
    @PreAuthorize("hasAnyRole('USER', 'OWNER')")
    @GetMapping("/reservation")
    public String reserveDiner(@RequestParam("id") Long id,
                               Authentication authentication,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        // 권한 확인
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        // if Owner
        boolean isOwner = authentication.getAuthorities().stream()
                .anyMatch(a -> Objects.requireNonNull(a.getAuthority()).equals(AuthorityStatus.ROLE_OWNER.getCode()));

        if (isOwner) {
            log.info("점주 계정의 예약 접근이 차단되었습니다.");
            redirectAttributes.addFlashAttribute("errorMessage", "점주 계정으로는 예약을 진행할 수 없습니다.");
            return "redirect:/";
        }

        try {
            // 뷰 렌더링에 데이터 조회
            DinerDetailDto diner = dinerService.getDinerById(id);
            Member member = memberService.getMember(authentication.getName());

            model.addAttribute("diner", diner);
            model.addAttribute("member", member);
            model.addAttribute("dinerId", id);

            return "reservation/reservation";
        } catch (Exception e) {
            log.error("예약 페이지 호출 중 에러 발생: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "식당 정보를 불러오는 중 오류가 발생했습니다.");
            return "redirect:/diner/list?category=전체";
        }
    }
}
