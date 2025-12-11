package com.example.demo.service.impl;

import org.springframework.beans.factory.annotation.Value;
import com.example.demo.service.GeocodingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeocodingServiceImpl implements GeocodingService {
    @Value("${naver.client.id}")
    private String clientId;

    @Value("${naver.client.secret}")
    private String clientSecret;

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper;

    @Override
    public Map<String, Double> getCoordinates(String address) {
        Map<String, Double> result = new HashMap<>();
        try{
            String url = "https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=" + address;
            String response = restClient.get()
                    .uri(url)
                    .header("X-NCP-APIGW-API-KEY-ID", clientId)
                    .header("X-NCP-APIGW-API-KEY", clientSecret)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode addresses = root.path("addresses");
            if (addresses.isArray() && addresses.size() > 0) {
                JsonNode firstResult = addresses.get(0);
                result.put("x", firstResult.path("x").asDouble());
                result.put("y", firstResult.path("y").asDouble());
            } else {
                result.put("x", 0.0);
                result.put("y", 0.0);
            }
            } catch (Exception e) {
            log.error("Geocoding API error: {}", e.getMessage());
            result.put("x", 0.0);
            result.put("y", 0.0);

        } return result;
    }
}
