package com.example.demo.service.impl;

import com.example.demo.data.dto.CoordinateDto;
import com.example.demo.data.enums.DinerStatus;
import com.example.demo.data.model.Diner;
import com.example.demo.data.repository.DinerRepository;
import com.example.demo.service.DinerExcelService;
import com.example.demo.service.GeocodingService;
import com.example.demo.service.NaverApiService;
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
    private final NaverApiService naverApiService;

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

        // 파일이 엑셀 파일이 아닐때 예외 처리
        String contentType =  file.getContentType();
        boolean isExcelMime = "application/vnd.ms-excel".equals(contentType) ||
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(contentType);

        if(!isExcelMime){
            throw new IllegalArgumentException("엑셀 파일이 아닙니다.");
        }

        //try-with-resources 구문으로 saveAll에서 오류 발생 시 자원 자동 해제
        try(InputStream inputStream = file.getInputStream();
        Workbook workbook = new XSSFWorkbook(inputStream);) {

            Sheet sheet = workbook.getSheetAt(0);
            // 엑셀 파일에 데이터가 없을 때
            if (sheet.getPhysicalNumberOfRows() <= 1) {
                throw new IllegalArgumentException("엑셀 파일 내에 추가할 데이터가 없습니다.");
            }
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

                //기존 데이터와 중복 확인
                if(dinerRepository.existsByDinerNameAndLocation(dinerName, location)){
                    log.info("이미 존재하는 식당 {}/{}, 업로드 건너뜀", dinerName, location);
                    continue; //중복 업로드 방지
                }

                //출장조리 장례식장 및 일부 식당 아닌 곳 제외 제외
                if (category.equals("출장조리") || containsKeyword(dinerName, "장례식장", "시스템", "푸드")){
                  continue;
                }

                //카테고리 재분류
                String finalCategory = refineCategory(category, dinerName);

                if ("기타".equals(finalCategory)) {
                    // 검색 정확도를 위해 "주소 앞부분 + 식당이름" 조합 (예: "달동 스타벅스")
                    String queryAddress = location.length() > 6 ? location.substring(0, 6) : location;
                    String searchQ = queryAddress + " " + dinerName;

                    String naverCat = naverApiService.searchCategory(searchQ);

                    if (naverCat != null) {
                        // 네이버 결과를 우리 카테고리로 매핑
                        finalCategory = mapNaverCategory(naverCat);
                        log.info("[API 보정] {} -> 네이버: {} -> 결과: {}", dinerName, naverCat, finalCategory);

                        // API 호출 너무 빠르면 차단되므로 딜레이
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            log.warn("API 대기 중 인터럽트 발생, 작업을 중단합니다.");
                            return;
                        }
                    }
                }

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
                            .status(DinerStatus.CLOSED)
                            .build();

                    dinerList.add(diner);

                    //API 호출 제한 방지를 위한 딜레이
                    Thread.sleep(100);
                } catch (Exception e) {
                    log.error("Geocoding failed for location [{}]: {}", location, e.getMessage());
                }
            }

            if (dinerList.isEmpty()) {
                throw new IllegalArgumentException("유효한 데이터가 존재하지 않습니다.");
            }

            dinerRepository.saveAll(dinerList);
            workbook.close();
            log.info("업로드 완료: {}건", dinerList.size());
        }
    }

    //카테고리 재분류
    private String refineCategory(String category, String dinerName) {
        if (category == null) category = "";
        if (dinerName == null) dinerName = "";

        String rawCat = category.trim();
        String name = dinerName.replaceAll("\\s+", ""); // 공백 제거

        // 1. [최우선] 카페/디저트 (오분류 1순위 해결)
        // 파일 분석 결과 '에스프레소', '로스팅' 같은 단어도 카페로 분류해야 함
        if (containsKeyword(name,
                "카페", "커피", "로스터스", "에스프레소", "베이커리", "빵집", "디저트", "다방",
                "투썸", "스벅", "스타벅스", "이디야", "메가MGC", "컴포즈", "빽다방", "할리스", "공차", "설빙", "폴바셋",
                "도넛", "케이크", "와플", "마카롱", "샌드위치", "토스트", "브런치")) {
            return "카페/디저트";
        }

        // 2. [이름 우선] 패스트푸드/피자/치킨 -> 양식으로 통합
        if (containsKeyword(name,
                "버거", "맥도날드", "버거킹", "롯데리아", "맘스터치", "프랭크",
                "피자", "도미노", "피자헛",
                "치킨", "통닭", "비비큐", "bhc", "교촌", "굽네", "처갓집", "강정")) {
            return "양식";
        }

        // 3. [이름 우선] 술집
        if (containsKeyword(name,
                "호프", "비어", "맥주", "포차", "주막", "주점", "이자카야", "펍", "pub", "bar", "와인", "칵테일", "노래연습장")) {
            return "술집/바";
        }

        // 4. [이름 우선] 일식/해산물 보정 ('돈가츠'는 일식으로, '쭈꾸미'는 해산물로)
        if (containsKeyword(name, "스시", "초밥", "카츠", "가츠", "우동", "소바", "라멘", "참치", "오마카세")) return "일식";
        if (containsKeyword(name, "횟집", "수산", "어시장", "쭈구미", "낙지", "오징어", "게장", "아구", "해물", "게찜")) return "횟집/해산물";

        // 5. 공공데이터 '카테고리' 컬럼 신뢰
        if (containsKeyword(rawCat, "김밥", "분식", "한식", "냉면", "국수")) return "한식/분식";
        if (containsKeyword(rawCat, "횟집", "복어", "해물", "생선회")) return "횟집/해산물";
        if (containsKeyword(rawCat, "중국식")) return "중식";
        if (containsKeyword(rawCat, "일식")) return "일식";
        if (containsKeyword(rawCat, "식육", "숯불", "고기")) return "고기/구이";

        // '경양식'은 위에서 카페를 다 걸러냈으므로, 여기 남은건 진짜 양식(돈가스/파스타)일 확률 높음
        if (containsKeyword(rawCat, "경양식", "패밀리레스토랑", "뷔페")) return "양식";
        if (containsKeyword(rawCat, "외국음식")) return "아시안";

        // 6. [마지막 보루] 이름으로 재추론
        if (containsKeyword(name, "국밥", "해장국", "식당", "밥상", "찌개", "두부", "면옥", "회관", "정식", "밥집")) return "한식/분식";
        if (containsKeyword(name, "갈비", "삼겹", "고기", "뒷고기", "막창", "곱창", "대패", "한우")) return "고기/구이";
        if (containsKeyword(name, "반점", "마라", "짬뽕", "짜장", "탕후루")) return "중식";
        if (containsKeyword(name, "파스타", "스테이크", "키친", "그릴", "레스토랑")) return "양식";
        if (containsKeyword(name, "쌀국수", "타이", "베트남", "인도", "커리")) return "아시안";

        return "기타"; // 여기까지 오면 진짜 기타이거나 API 검색 대상
    }
    // ──────────────────────────────────────────────
    // [핵심] 2차 분류: 네이버 API 결과를 내 카테고리로 매핑
    // ──────────────────────────────────────────────
    private String mapNaverCategory(String naverCat) {
        if (naverCat == null) return "기타";

        if (naverCat.contains("카페") || naverCat.contains("디저트") || naverCat.contains("베이커리")) return "카페/디저트";
        if (naverCat.contains("고기") || naverCat.contains("육류") || naverCat.contains("곱창")) return "고기/구이";
        if (naverCat.contains("회") || naverCat.contains("해물") || naverCat.contains("수산물")) return "횟집/해산물";
        if (naverCat.contains("일식") || naverCat.contains("초밥")) return "일식";
        if (naverCat.contains("중식") || naverCat.contains("중국")) return "중식";
        if (naverCat.contains("이탈리아") || naverCat.contains("양식") || naverCat.contains("피자") || naverCat.contains("햄버거")) return "양식";
        if (naverCat.contains("술집") || naverCat.contains("주점") || naverCat.contains("포차") || naverCat.contains("바(BAR)")) return "술집/바";
        if (naverCat.contains("아시아") || naverCat.contains("베트남") || naverCat.contains("태국")) return "아시안";
        if (naverCat.contains("한식") || naverCat.contains("분식") || naverCat.contains("국밥")) return "한식/분식";

        return "기타";
    }
    public boolean containsKeyword(String target, String... keywords) {
      for (String k : keywords) {
        if (target.contains(k)) return true;
      }
      return false;
    }
}
