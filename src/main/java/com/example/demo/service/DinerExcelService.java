package com.example.demo.service;

import org.apache.poi.ss.usermodel.Cell;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface DinerExcelService {

    String getCellValue(Cell cell);

    void uploadExcel(MultipartFile file) throws IOException;

    String refineCategory(String category, String dinerName);

    String mapNaverCategory(String naverCat);

    boolean containsKeyword(String target, String... keywords);
}
