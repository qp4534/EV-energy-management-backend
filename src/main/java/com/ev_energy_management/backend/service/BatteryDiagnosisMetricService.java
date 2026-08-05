package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.dto.BatteryDiagnosisMetricDto;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BatteryDiagnosisMetricService {

    private List<BatteryDiagnosisMetricDto> mockData() {
        return List.of(
                new BatteryDiagnosisMetricDto(UUID.randomUUID(), 88, 91, 85, 90, OffsetDateTime.now(), UUID.randomUUID()),
                new BatteryDiagnosisMetricDto(UUID.randomUUID(), 64, 70, 58, 66, OffsetDateTime.now(), UUID.randomUUID())
        );
    }

    public List<BatteryDiagnosisMetricDto> findAll() {
        return mockData();
    }

    public BatteryDiagnosisMetricDto findById(UUID metricId) {
        return new BatteryDiagnosisMetricDto(metricId, 88, 91, 85, 90, OffsetDateTime.now(), UUID.randomUUID());
    }

    public BatteryDiagnosisMetricDto create(BatteryDiagnosisMetricDto request) {
        return new BatteryDiagnosisMetricDto(UUID.randomUUID(), request.remainingLifeScore(), request.dischargePowerScore(),
                request.chargeHealthScore(), request.voltageStabilityScore(), OffsetDateTime.now(), request.batteryId());
    }

    public BatteryDiagnosisMetricDto update(UUID metricId, BatteryDiagnosisMetricDto request) {
        return new BatteryDiagnosisMetricDto(metricId, request.remainingLifeScore(), request.dischargePowerScore(),
                request.chargeHealthScore(), request.voltageStabilityScore(), request.diagnosedAt(), request.batteryId());
    }

    public void delete(UUID metricId) {
        // TODO: ERD 확정 후 실제 삭제 로직 연결
    }
}
