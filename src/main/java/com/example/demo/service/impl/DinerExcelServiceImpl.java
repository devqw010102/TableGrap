package com.example.demo.service.impl;

import com.example.demo.data.dto.CoordinateDto;
import com.example.demo.data.enums.DinerStatus;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class DinerExcelServiceImpl implements DinerExcelService {
    private final DinerRepository dinerRepository;
    private final GeocodingService geocodingService;

    //getCellValue 메소드 정의
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
    /* 식당 데이터 파일의 카테고리 부분이 매우 자유분방하여 기존 원본 카테고리를 하위 카테고리로 지정하여
    *  상위 카테고리 10가지로 재분류 했습니다. 기타로 기재된 부분은 상호명으로 다시 분류하고
    *  상호명이 모호한 식당은 일단은 기타로 분류했습니다.
    */
    @Override
    @Transactional
    public void uploadExcel(MultipartFile file) throws IOException {
        //try-with-resources 구문으로 saveAll에서 오류 발생 시 자원 자동 해제
        try(InputStream inputStream = file.getInputStream();
        Workbook workbook = new XSSFWorkbook(inputStream);) {

            Sheet sheet = workbook.getSheetAt(0);
            List<Diner> dinerList = new ArrayList<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue; //행이 비었을 경우 건너뜀

                //폐업 여부 확인하고 제외(인덱스 확인 필요)
                String businessStatus = getCellValue(row.getCell(8));
                if (businessStatus.contains("폐업")) {
                    continue;
                }

                //데이터 추출
                String category = getCellValue(row.getCell(25)); //카테고리
                String location = getCellValue(row.getCell(19)); //주소
                String dinerName = getCellValue(row.getCell(21)); //식당 이름
                String tel = getCellValue(row.getCell(15));       //전화번호

                if (location == null || location.isEmpty()) {
                    continue; // 위치 정보가 없으면 건너뜀
                }
                //출장조리 제외
                if (category.equals("출장조리")){
                  continue;
                }
                //장례식장 및 일부 식당 아닌 곳 제외 제외
                if(containsKeyword(dinerName, "장례식장", "시스템")){
                  continue;
                }
                String finalCategory = refineCategory(category, dinerName);

                //인터페이스 메소드 호출
                try {
                    CoordinateDto coords = geocodingService.getCoordinates(location);
                    Diner diner = Diner.builder()
                            .category(finalCategory)
                            .location(location)
                            .dinerName(dinerName)
                            .tel(tel.isEmpty() ? null : tel)
                            .dx(coords.getDx())
                            .dy(coords.getDy())
                            .status(DinerStatus.PUBLIC)
                            .build();

                    dinerList.add(diner);

                    //API 호출 제한 방지를 위한 딜레이
                    Thread.sleep(100);
                } catch (Exception e) {
                    log.error("Geocoding failed for location [{}]: {}", location, e.getMessage());
                }
            }

            dinerRepository.saveAll(dinerList);
            workbook.close();
            log.info("업로드 완료: {}건", dinerList.size());
        }
    }

    //카테고리 재분류
    public String refineCategory(String category, String dinerName) {
      //null방지
      if(category == null) category="";
      if(dinerName == null) dinerName="";

      String rawCat = category.trim();
      String name = dinerName.replaceAll("\\s+", "");//공백제거
      //하위카테고리를분류
      if(containsKeyword(rawCat, "한식", "김밥", "냉면집", "분식", "탕류")) return "한식/분식";
      if(containsKeyword(rawCat, "횟집", "복어취급", "초장집")) return "횟집/해산물";
      if(containsKeyword(rawCat, "경양식", "패밀리레스토랑", "뷔페식")) return "양식";
      if(containsKeyword(rawCat, "까페", "라이브카페", "전통찻집", "키즈카페")) return "카페/디저트";
      if(containsKeyword(rawCat, "감성주점", "정종/대포집/소주방", "호프/통닭")) return "술집/바";
      
      if(rawCat.equals("일식")) return "일식";
      if(rawCat.equals("중국식")) return "중식";
      if(rawCat.equals("외국음식전문점(인도, 태국 등")) return "아시안";
      if(rawCat.equals("식육(숯불구이)")) return "고기/구이";

      //기타부분 식당이름으로 재분류
      if(rawCat.equals("기타")) {
        if (containsKeyword(name, "감자탕", "해장국", "식당", "비빔밥", "분식", "떡볶이", "국밥", "칼국수", "국수")) return "한식/분식";
        if (containsKeyword(name, "일식", "타코야끼", "카츠", "연어")) return "일식";
        if (containsKeyword(name, "반점", "짜장", "짬뽕", "마라탕")) return "중식";
        if (containsKeyword(name, "장어", "복집", "매운탕", "쭈구미", "낙지")) return "횟집/해산물";
        if (containsKeyword(name, "피자", "버거", "스테이크", "치킨")) return "양식";
        if (containsKeyword(name, "오리", "닭갈비", "뒷고기", "삼겹살", "숯불", "육회", "뭉티기", "식육", "갈비", "곱창", "막창")) return "고기/구이";
        if (containsKeyword(name, "카레", "미스사이공", "쌀국수")) return "아시안";
        if (containsKeyword(name, "커피", "케이크", "마카롱", "브레드", "투썸플레이스", "베이커리", "해월당", "베이글", "랑콩뜨레", "파이")) return "카페/디저트";
        if (containsKeyword(name, "맥주", "선술", "펍", "주점", "처음처럼", "술집", "호프", "투다리", "간이역", "통닭")) return "술집/바";
      }
      return "기타"; //식당이름으로도 분류하기 모호한 데이터는 기타로 분류
    }
    public boolean containsKeyword(String target, String... keywords) {
      for (String k : keywords) {
        if (target.contains(k)) return true;
      }
      return false;
    }
}
