package com.example.demo.service;

import java.util.Map;

public interface GeocodingService {
    Map<String, Double> getCoordinates(String address);
}
