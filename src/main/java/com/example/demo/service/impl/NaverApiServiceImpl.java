package com.example.demo.service.impl;

import com.example.demo.service.NaverApiService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverApiServiceImpl implements NaverApiService {

    @Value("${naver.search.client.id}") // application.properties에 설정 필수
    private String clientId;

    @Value("${naver.search.client.secret}") // application.properties에 설정 필수
    private String clientSecret;

    @Override
    public String searchCategory(String query) {
        if (clientId == null || clientSecret == null) {
            log.warn("네이버 API 키가 설정되지 않아 검색을 건너뜁니다.");
            return null;
        }

        try {
            URI uri = UriComponentsBuilder
                    .fromUriString("https://openapi.naver.com")
                    .path("/v1/search/local.json")
                    .queryParam("query", query)
                    .queryParam("display", 1) // 가장 정확한 1개만
                    .queryParam("sort", "random")
                    .encode(StandardCharsets.UTF_8)
                    .build()
                    .toUri();

            RestTemplate restTemplate = new RestTemplate();
            RequestEntity<Void> req = RequestEntity
                    .get(uri)
                    .header("X-Naver-Client-Id", clientId)
                    .header("X-Naver-Client-Secret", clientSecret)
                    .build();

            NaverSearchDto result = restTemplate.exchange(req, NaverSearchDto.class).getBody();

            if (result != null && result.getItems() != null && !result.getItems().isEmpty()) {
                String rawCategory = result.getItems().get(0).getCategory();
                // HTML 태그 제거 및 불필요한 공백 제거
                return rawCategory.replaceAll("<[^>]*>", "").trim();
            }

        } catch (Exception e) {
            log.error("네이버 API 호출 실패 [query={}]: {}", query, e.getMessage());
        }
        return null;
    }

    // 내부 DTO 클래스
    @Data
    public static class NaverSearchDto {
        private List<Item> items;
        @Data
        public static class Item {
            private String title;
            private String category;
        }
    }
}
