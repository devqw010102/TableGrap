package com.example.demo.service.impl;

import com.example.demo.data.dto.CoordinateDto;
import org.springframework.beans.factory.annotation.Value;
import com.example.demo.service.GeocodingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
//Spring Boot 3.x 이상에서는 tools.jackson.databind.JsonNode 사용
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;


import java.net.URI;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeocodingServiceImpl implements GeocodingService {
    @Value("${naver.client.id}")
    private String clientId;

    @Value("${naver.client.secret}")
    private String clientSecret;

    private final RestClient restClient = RestClient.create();
    private final JsonMapper jsonMapper;

    @Override
    public CoordinateDto getCoordinates(String address) {
        //초기값 설정 및 실패시 반환값
        CoordinateDto coordinateDto =CoordinateDto.builder()
                .dx(0.0)
                .dy(0.0)
                .build();

        try{//한글 주소 깨짐 방지 위해 uri빌더 사용
            URI uri = UriComponentsBuilder
                    .fromUriString("https://maps.apigw.ntruss.com/map-geocode/v2/geocode")
                    .queryParam("query", address)
                    .encode(StandardCharsets.UTF_8) // 한글 주소 깨짐 방지
                    .build()
                    .toUri();
            //API 호출
            String response = restClient.get()
                    .uri(uri)
                    .header("X-NCP-APIGW-API-KEY-ID", clientId)
                    .header("X-NCP-APIGW-API-KEY", clientSecret)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);
            //API응답을 JSON으로 파싱  
            JsonNode root = jsonMapper.readTree(response);
            JsonNode addresses = root.path("addresses");

            //주소가 배열이면서 비어있지 않은 경우 좌표 추출
            if (addresses.isArray() && !addresses.isEmpty()) {
                JsonNode firstResult = addresses.get(0);
                coordinateDto = CoordinateDto.builder()
                        .dx(firstResult.path("x").asDouble()) //경도
                        .dy(firstResult.path("y").asDouble()) //위도
                        .build();

            }
            } catch (Exception e) {
            log.error("Geocoding API error extracting coordinate of [{}]: {}", address, e.getMessage());
        }
        return coordinateDto;
    }
}
