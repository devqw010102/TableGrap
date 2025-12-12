package com.example.demo.service;

import com.example.demo.data.dto.CoordinateDto;

public interface GeocodingService {
    //Map타입에서 CoordinateDto로 변경 Map타입은 String으로 관리함으로 오타 발생 가능성 있음
    CoordinateDto getCoordinates(String address);
}
