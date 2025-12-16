package com.example.demo.service;

import com.example.demo.data.dto.DinerDetailDto;
import com.example.demo.data.dto.DinerListDto;
import com.example.demo.data.model.Diner;
import java.util.List;

public interface DinerService {
    public List<Diner> getList();
    DinerDetailDto getDinerById(Long id);
    public List<DinerListDto> getListByCat(String category);
}
