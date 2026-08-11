package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.client.ChargingDemandClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/charging-demand")
public class ChargingDemandController {

    private final ChargingDemandClient chargingDemandClient;

    public ChargingDemandController(ChargingDemandClient chargingDemandClient) {
        this.chargingDemandClient = chargingDemandClient;
    }

    // 지도 화면 "지금 시간대 충전 수요" 배지 전용. hour/dow/month는 기기의 로컬 시각 기준으로
    // 클라이언트가 계산해서 보내고, 연도는 모델 학습 범위에 맞게 서버에서 대체해서 호출한다.
    @GetMapping("/current")
    public Map<String, Object> getCurrentDemand(
            @RequestParam int hour, @RequestParam int dow, @RequestParam int month) {
        return chargingDemandClient.predict(hour, dow, month);
    }
}
