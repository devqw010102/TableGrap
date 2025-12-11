package com.example.demo.controller;

import com.example.demo.service.DinerExcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class AdminController {
    private final DinerExcelService dinerExcelService;

    @PostMapping("/admin/upload-diner")
    public String uploadDinerExcel(@RequestParam("file") MultipartFile file) {
        try {
            dinerExcelService.uploadExcel(file);
            return "Upload successful";
        } catch (Exception e) {
            return "Upload failed: " + e.getMessage();
        }
    }
}
