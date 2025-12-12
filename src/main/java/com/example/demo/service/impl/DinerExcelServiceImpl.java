package com.example.demo.service.impl;

import com.example.demo.data.model.Diner;
import com.example.demo.data.repository.DinerRepository;
import com.example.demo.service.DinerExcelService;
import com.example.demo.service.GeocodingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DinerExcelServiceImpl implements DinerExcelService {
    private final DinerRepository dinerRepository;
    private final GeocodingService geocodingService;

    @Override
    @Transactional
    public void uploadExcel(MultipartFile file) throws IOException {
        InputStream inputStream = file.getInputStream();
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        List<Diner> dinerList = new ArrayList<>();

        for(int i = 1; i<= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            //폐업 여부 확인하고 제외(인텍스 확인 필요)
            String businessStatus = row.getCell(8).getStringCellValue();
            if(businessStatus.contains("폐업")) {
                continue;
            }

            //데이터 추출
            String category = getCellValue(row.getCell(25));
            String location = getCellValue(row.getCell(19));
            String dinerName = getCellValue(row.getCell(21));
            String tel = getCellValue(row.getCell(15));

            if (location == null || location.isEmpty()) {
                continue; // 위치 정보가 없으면 건너뜀
            }

            //인터페이스 메소드 호출
            try{
                Map<String, Double> cords = geocodingService.getCoordinates(location);
                Diner diner = Diner.builder()
                        .category(category)
                        .location(location)
                        .dinerName(dinerName)
                        .tel(tel)
                        .dx(cords.get("x"))
                        .dy(cords.get("y"))
                        .build();

                dinerList.add(diner);

                //API 호출 제한 방지를 위한 딜레이
                Thread.sleep(50);
            } catch (Exception e) {
                log.error("Geocoding failed for location");
            }
        }
        dinerRepository.saveAll(dinerList);
        workbook.close();
        log.info("업로드 완료: {}건", dinerList.size());
            }
    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            default: return "";
        }
    }
}
