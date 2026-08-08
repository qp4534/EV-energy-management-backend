package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.client.RulDiagnosisPdfClient;
import com.ev_energy_management.backend.dto.BatteryOfferDto;
import com.ev_energy_management.backend.dto.battery.BuyerDisclosureRequest;
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

    // "배터리 잔존가치/판매처" 탭의 매입처 카드에서 "실시간 검색" 버튼을 눌렀을 때 호출.
    // 이 매입처가 사용후 배터리를 매입하겠다고 공개적으로 밝힌 근거자료를 찾아 요약해온다.
    // 검색 결과가 없거나 실패해도 항상 200으로 응답하고(disclosure: null), 화면은 기존
    // DB 문구로 그대로 폴백한다 - 이 부가 기능 하나 때문에 화면이 에러로 막히면 안 된다.
    @PostMapping("/buyer-disclosure")
    public Map<String, String> getBuyerDisclosure(@RequestBody BuyerDisclosureRequest request) {
        String disclosure = rulDiagnosisPdfClient.fetchBuyerDisclosure(request);
        Map<String, String> body = new java.util.HashMap<>();
        body.put("disclosure", disclosure);
        return body;
    }

    // "배터리 잔존가치/판매처" 탭의 "실시간 검색으로 매입처 확인" 버튼 전용. 매입처를
    // DB 고정값이 아니라 실시간 검색으로 찾아서(회사만) 목록을 다시 구성하고, 가격은
    // rul-diagnosis 쪽 기존 계산식(출처 있는 벤치마크)을 그대로 적용해 돌려준다.
    @PostMapping("/live-offers")
    public Map<String, Object> getLiveOffers(@RequestBody LiveOffersRequest request) {
        return rulDiagnosisPdfClient.fetchLiveOffers(request);
    }
}
