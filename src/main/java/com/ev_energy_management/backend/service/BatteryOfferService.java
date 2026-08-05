package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.BatteryOfferDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class BatteryOfferService {

    private List<BatteryOfferDto> mockData() {
        return List.of(
                new BatteryOfferDto(UUID.randomUUID(), "그린리사이클㈜", "재활용업체", new BigDecimal("7800000.00"),
                        new BigDecimal("100000.00"), 1, "즉시 매입 가능합니다.", UUID.randomUUID()),
                new BatteryOfferDto(UUID.randomUUID(), "이엔지배터리", "재제조업체", new BigDecimal("7200000.00"),
                        new BigDecimal("93000.00"), 2, "검수 후 매입 진행.", UUID.randomUUID())
        );
    }

    public List<BatteryOfferDto> findAll() {
        return mockData();
    }

    public BatteryOfferDto findById(UUID offerId) {
        return new BatteryOfferDto(offerId, "그린리사이클㈜", "재활용업체", new BigDecimal("7800000.00"),
                new BigDecimal("100000.00"), 1, "즉시 매입 가능합니다.", UUID.randomUUID());
    }

    public BatteryOfferDto create(BatteryOfferDto request) {
        return new BatteryOfferDto(UUID.randomUUID(), request.buyerName(), request.businessType(),
                request.offeredPrice(), request.pricePerKwh(), request.rankOrder(), request.description(), request.batteryId());
    }

    public BatteryOfferDto update(UUID offerId, BatteryOfferDto request) {
        return new BatteryOfferDto(offerId, request.buyerName(), request.businessType(),
                request.offeredPrice(), request.pricePerKwh(), request.rankOrder(), request.description(), request.batteryId());
    }

    public void delete(UUID offerId) {
        // TODO: ERD 확정 후 실제 삭제 로직 연결
    }
}
