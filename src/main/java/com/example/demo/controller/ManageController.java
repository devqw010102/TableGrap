package com.example.demo.controller;

import com.example.demo.service.DinerExcelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/manage")
@RequiredArgsConstructor
@Slf4j
public class ManageController {

    private final DinerExcelService dinerExcelService;

    @PostMapping("/upload")
    public String createDiner(@RequestParam("file")MultipartFile file, RedirectAttributes redirectAttributes) throws IOException {
        // errorMessage 는 script 로 처리
        // 빈 파일 일때
        if(file.isEmpty()){
            redirectAttributes.addFlashAttribute("errorMessage", "업로드할 파일을 선택하세요");
            return "redirect:/admin";
        }

        try {
            dinerExcelService.uploadExcel(file);
            redirectAttributes.addFlashAttribute("successMessage", "데이터 업로드가 완료되었습니다.");

        } catch (IllegalArgumentException e) {
            // 확장자명이 엑셀이 아닐 때
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "파일 읽기 중 오류가 발생했습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "서버 내부 오류가 발생했습니다.");
        }

        // upload 구현

        return "redirect:/admin";
    }
}
