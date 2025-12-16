package com.example.demo.controller;

import com.example.demo.data.dto.DinerListDto;
import com.example.demo.data.model.Diner;
import com.example.demo.service.DinerService;
import com.example.demo.service.impl.DinerServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/diner")
public class DinerListController {
  private final DinerService dinerService;

  //카테고리별로 일일이 매핑하는 것은 비효율적이므로 쿼리 파라미터 사용
  @GetMapping("/list")
  public String getDiner(@PageableDefault(size = 20, sort="id", direction = Sort.Direction.ASC) Pageable pageable,
                         @RequestParam("category") String category, Model model){
    Page<DinerListDto> page = dinerService.getListByCat(pageable, category);
    model.addAttribute("dinerList", page);
    model.addAttribute("categoryName", category);
    return "dinerList/dinerList";
  }

}
