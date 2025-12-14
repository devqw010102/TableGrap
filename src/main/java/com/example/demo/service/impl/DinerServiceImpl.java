package com.example.demo.service.impl;

import com.example.demo.data.model.Diner;
import com.example.demo.data.repository.DinerRepository;
import com.example.demo.service.DinerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DinerServiceImpl implements DinerService {

    private final DinerRepository dinerRepository;

    public List<Diner> getList() {
        return dinerRepository.findAll();
    }

    @Override
    public Diner getDinerById(Long id) {
        // DB에서 ID로 찾고, 없으면 에러
        return dinerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 식당이 없습니다. id=" + id));
    }
}
