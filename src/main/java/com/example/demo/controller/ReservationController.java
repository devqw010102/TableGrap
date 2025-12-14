package com.example.demo.controller;

import com.example.demo.data.model.Diner;
import com.example.demo.service.DinerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ReservationController {
    @Autowired
    private DinerService dinerService;
    // api
    @Value("${naver.client.id}")
    private String naverClientId;

    @GetMapping("/reservation")
    public String reservationPage(@RequestParam(value = "id", required = false) Long id, Model model) {

        // ID가 없으면 기본값 1번 식당 보여주기...? 404?
        if (id == null) {
            id = 1L;
        }
        // DB에서 식당 정보 가져오기
        Diner diner = dinerService.getDinerById(id);

        // HTML로 데이터 보내기
        model.addAttribute("diner", diner);
        model.addAttribute("naverClientId", naverClientId); // API 키도

        return "reservation/reservation";
    }
}
