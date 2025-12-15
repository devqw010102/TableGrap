package com.example.demo.service;

import com.example.demo.data.dto.DinerListDto;
import com.example.demo.data.model.Diner;
import java.util.List;

public interface DinerService {
    public List<Diner> getList();
    //merge pull request 충돌방지용으로 복붙, merge이후 해당 주석 삭제
    Diner getDinerById(Long id);
    public List<DinerListDto> getListByCat(String category);
}
