package com.example.demo.controller;

import com.example.demo.data.model.Diner;
import com.example.demo.service.DinerExcelService;
import com.example.demo.service.DinerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/manage")
@RequiredArgsConstructor
public class ManageController {

    private final DinerService dinerService;
    private final DinerExcelService dinerExcelService;

    @PostMapping("/upload")
    public String createDiner(@RequestParam("file")MultipartFile file, Model model) throws IOException {
        // upload 구현

        dinerExcelService.uploadExcel(file);
        model.addAttribute("list", dinerService.getList());

        return "redirect:/admin";
    }

    @GetMapping("/upload")
    public String uploadPage(Model model) {
        model.addAttribute("list", dinerService.getList());
        return "redirect:/admin";
    }
}
