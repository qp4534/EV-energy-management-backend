package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.BatteryPassportDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class BatteryPassportService {

    private List<BatteryPassportDto> mockData() {
        return List.of(
                new BatteryPassportDto(UUID.randomUUID(), "LG에너지솔루션", "리튬이온", "77.4kWh",
                        new BigDecimal("92.5"), 320, new BigDecimal("28.4"), LocalDate.now().minusMonths(1),
                        UUID.randomUUID(), "1", 680, 1000, "양호", "재사용(EV 재제조)급",
                        new BigDecimal("95.0"), "{\"unusable\":0.05,\"reusable\":0.9,\"remanufacturable\":0.05}"),
                new BatteryPassportDto(UUID.randomUUID(), "삼성SDI", "리튬이온", "58.0kWh",
                        new BigDecimal("78.2"), 610, new BigDecimal("31.1"), LocalDate.now().minusMonths(3),
                        UUID.randomUUID(), "2", 390, 1000, "노후", "2차사용(ESS)급",
                        new BigDecimal("80.0"), "{\"unusable\":0.15,\"reusable\":0.6,\"remanufacturable\":0.25}")
        );
    }

    public List<BatteryPassportDto> findAll() {
        return mockData();
    }

    public BatteryPassportDto findById(UUID batteryId) {
        return new BatteryPassportDto(batteryId, "LG에너지솔루션", "리튬이온", "77.4kWh",
                new BigDecimal("92.5"), 320, new BigDecimal("28.4"), LocalDate.now().minusMonths(1),
                UUID.randomUUID(), "1", 680, 1000, "양호", "재사용(EV 재제조)급",
                new BigDecimal("95.0"), "{\"unusable\":0.05,\"reusable\":0.9,\"remanufacturable\":0.05}");
    }

    public BatteryPassportDto create(BatteryPassportDto request) {
        return new BatteryPassportDto(UUID.randomUUID(), request.manufacturer(), request.batteryType(),
                request.ratedCapacity(), request.sohScore(), request.chargeCycles(), request.currentTemp(),
                request.lastInspectedAt(), request.carId(), request.batteryLevel(), request.remainingCycles(),
                request.totalCycles(), request.reuseStatus(), request.gradeDetail(), request.reliabilityScore(),
                request.reuseProbabilities());
    }

    public BatteryPassportDto update(UUID batteryId, BatteryPassportDto request) {
        return new BatteryPassportDto(batteryId, request.manufacturer(), request.batteryType(),
                request.ratedCapacity(), request.sohScore(), request.chargeCycles(), request.currentTemp(),
                request.lastInspectedAt(), request.carId(), request.batteryLevel(), request.remainingCycles(),
                request.totalCycles(), request.reuseStatus(), request.gradeDetail(), request.reliabilityScore(),
                request.reuseProbabilities());
    }

    public void delete(UUID batteryId) {
        // TODO: ERD 확정 후 실제 삭제 로직 연결
    }
}
