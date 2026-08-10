package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.client.RulDiagnosisPdfClient;
import com.ev_energy_management.backend.dto.BatteryOfferDto;
import com.ev_energy_management.backend.dto.battery.LiveOffersRequest;
import com.ev_energy_management.backend.service.BatteryOfferService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/battery-offers")
public class BatteryOfferController {

    private final BatteryOfferService batteryOfferService;
    private final RulDiagnosisPdfClient rulDiagnosisPdfClient;

    public BatteryOfferController(BatteryOfferService batteryOfferService,
                                   RulDiagnosisPdfClient rulDiagnosisPdfClient) {
        this.batteryOfferService = batteryOfferService;
        this.rulDiagnosisPdfClient = rulDiagnosisPdfClient;
    }

    @GetMapping
    public List<BatteryOfferDto> getBatteryOffers() {
        return batteryOfferService.findAll();
    }

    @GetMapping("/{offerId}")
    public BatteryOfferDto getBatteryOffer(@PathVariable UUID offerId) {
        return batteryOfferService.findById(offerId);
    }

    @PostMapping
    public ResponseEntity<BatteryOfferDto> createBatteryOffer(@RequestBody BatteryOfferDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(batteryOfferService.create(request));
    }

    @PutMapping("/{offerId}")
    public BatteryOfferDto updateBatteryOffer(@PathVariable UUID offerId, @RequestBody BatteryOfferDto request) {
        return batteryOfferService.update(offerId, request);
    }

    @DeleteMapping("/{offerId}")
    public ResponseEntity<Void> deleteBatteryOffer(@PathVariable UUID offerId) {
        batteryOfferService.delete(offerId);
        return ResponseEntity.noContent().build();
    }

    // "배터리 잔존가치/판매처" 탭 로드 시 호출. 매입처를 DB 고정값이 아니라 실시간 검색으로
    // 찾아서(회사만) 목록을 구성하고, 가격은 rul-diagnosis 쪽 기존 계산식(출처 있는
    // 벤치마크)을 그대로 적용해 돌려준다. Serper/DeepSeek 키는 서버 시크릿으로 자동 처리.
    @PostMapping("/live-offers")
    public Map<String, Object> getLiveOffers(@RequestBody LiveOffersRequest request) {
        return rulDiagnosisPdfClient.fetchLiveOffers(request);
    }
}
